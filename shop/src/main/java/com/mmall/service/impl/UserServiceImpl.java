package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * Created by geely
 */
@Service("iUserService")
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;


    @Override
    public ServerResponse<User> login(String username, String password) {
        int resultCount = userMapper.checkUsername(username);
        if(resultCount == 0 ){
            return ServerResponse.createByErrorMessage("用户名不存在");
        }


        String md5Password = MD5Util.MD5EncodeUtf8(password);//前端输入的密码进行加密，才能与数据库的匹配
        User user  = userMapper.selectLogin(username,md5Password);
        if(user == null){
            return ServerResponse.createByErrorMessage("密码错误");
        }

        user.setPassword(org.apache.commons.lang3.StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登录成功",user);
    }




    public ServerResponse<String> register(User user){
        /*int resultCount = userMapper.checkUsername(user.getUsername());
        if(resultCount == 0 ){
            return ServerResponse.createByErrorMessage("用户名不存在");
        }  用下面的代替  */
        ServerResponse validResponse = this.checkValid(user.getUsername(),Const.USERNAME);
        if(!validResponse.isSuccess()){
            return validResponse;  //没有校验通过返回这个
        }
        validResponse = this.checkValid(user.getEmail(),Const.EMAIL);
        if(!validResponse.isSuccess()){
            return validResponse;
        }//查了用户和邮箱说明你已经注册过了
        user.setRole(Const.Role.ROLE_CUSTOMER);  //角色默认设置为普通用户0
        //MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        int resultCount = userMapper.insert(user);
        if(resultCount == 0){
            return ServerResponse.createByErrorMessage("注册失败");
        }
        return ServerResponse.createBySuccessMessage("注册成功");
    }

    public ServerResponse<String> checkValid(String str,String type){//校验   str为value 值    type要么用户名验证要么邮箱验证
        if(org.apache.commons.lang3.StringUtils.isNotBlank(type)){ //type不为空时
            //开始校验
            if(Const.USERNAME.equals(type)){
                int resultCount = userMapper.checkUsername(str);
                if(resultCount > 0 ){
                    return ServerResponse.createByErrorMessage("用户名已存在");
                }
            }
            if(Const.EMAIL.equals(type)){
                int resultCount = userMapper.checkEmail(str);
                if(resultCount > 0 ){
                    return ServerResponse.createByErrorMessage("email已存在");
                }
            }
        }else{
            return ServerResponse.createByErrorMessage("参数错误");
        }
        return ServerResponse.createBySuccessMessage("校验成功");
    }

    // 获取用户登录信息
    public ServerResponse selectQuestion(String username){

        ServerResponse validResponse = this.checkValid(username,Const.USERNAME);//调用上方的校验用户
        if(validResponse.isSuccess()){
            // 如果用户不存在所以显示校验成功  用户不存在
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String question = userMapper.selectQuestionByUsername(username); //select question
        if(org.apache.commons.lang3.StringUtils.isNotBlank(question)){
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByErrorMessage("找回密码的问题是空的");
    }

    //使用本地缓存检查问题答案
    public ServerResponse<String> checkAnswer(String username,String question,String answer){
        int resultCount = userMapper.checkAnswer(username,question,answer);  //select count(1) where
        if(resultCount>0){
            // 查询出来的结果大于0 说明问题及问题答案是这个用户的,并且是正确的
            String forgetToken = UUID.randomUUID().toString();//生成UUID   宇宙无敌不可重复的字符串
            TokenCache.setKey(TokenCache.TOKEN_PREFIX+username,forgetToken);  //key加一个前缀进行区分TokenCache.TOKEN_PREFIX，  本地的缓存Token放进去了
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("问题的答案错误");
    }


    //忘记密码中的重置密码
    public ServerResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken){
        if(org.apache.commons.lang3.StringUtils.isBlank(forgetToken)){//判断字符串为空时最好用org.apache.commons.lang3.isNotBlank,  而不是isEmpty
            return ServerResponse.createByErrorMessage("参数错误,token需要传递");
        }
        ServerResponse validResponse = this.checkValid(username,Const.USERNAME);
        if(validResponse.isSuccess()){
            //用户不存在
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX+username);
        if(org.apache.commons.lang3.StringUtils.isBlank(token)){//空
            return ServerResponse.createByErrorMessage("token无效或者过期");
        }
        // 若a==null if( "abc".equals(a) )  会报空指针异常  用StringUtils无需考虑这问题  null传进去也不报异常
        if(org.apache.commons.lang3.StringUtils.equals(forgetToken,token)){
            String md5Password  = MD5Util.MD5EncodeUtf8(passwordNew);  // update mmall_user SET password = #{passwordNew}  UserMapper用到参数是passwordNew

            int rowCount = userMapper.updatePasswordByUsername(username,md5Password);// 不能用原密码passwordNew  而是加密后的

            if(rowCount > 0){//生效函数》0
                return ServerResponse.createBySuccessMessage("修改密码成功");
            }
        }else{
            return ServerResponse.createByErrorMessage("token错误,请重新获取重置密码的token");
        }
        return ServerResponse.createByErrorMessage("修改密码失败"); //为什么这里返回的是修改失败？？？  因为如果修改成功 在139行就已经返回成功了
    }

    // 登录状态的重置密码
    public ServerResponse<String> resetPassword(String passwordOld,String passwordNew,User user){
        //防止横向越权,要校验一下这个用户的旧密码,一定要指定是这个用户.因为我们会查询一个count(1),如果不指定id,那么结果就是true啦count>0;
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld),user.getId());
        if(resultCount == 0){
            return ServerResponse.createByErrorMessage("旧密码错误");
        }
// select count(1) .. where password=#{password} and id = #{userId}
        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));//新密码的之先存到user对象
        int updateCount = userMapper.updateByPrimaryKeySelective(user);//根据id更新
        if(updateCount > 0){
            return ServerResponse.createBySuccessMessage("密码更新成功");
        }
        return ServerResponse.createByErrorMessage("密码更新失败");
    }

    //更新个人用户信息
    public ServerResponse<User> updateInformation(User user){
        //username是不能被更新的
        //email也要进行一个校验,校验新的email是不是已经存在,并且存在的email如果相同的话,不能是我们当前的这个用户的.
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(),user.getId());  //select count(1) where email = #{email} and id = #{userId} 统计查询出来符合条件的数量
        if(resultCount > 0){
            return ServerResponse.createByErrorMessage("email已存在,请更换email再尝试更新");
        }
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);//不等于空的时候进行更新
        if(updateCount > 0){
            return ServerResponse.createBySuccess("更新个人信息成功",updateUser);
        }
        return ServerResponse.createByErrorMessage("更新个人信息失败");
    }



    public ServerResponse<User> getInformation(Integer userId){
        User user = userMapper.selectByPrimaryKey(userId);
        if(user == null){
            return ServerResponse.createByErrorMessage("找不到当前用户");
        }
        user.setPassword(org.apache.commons.lang3.StringUtils.EMPTY);  //把密码值变空
        return ServerResponse.createBySuccess(user);

    }




    //backend

    /**
     * 校验是否是管理员
     * @param user
     * @return
     */
    public ServerResponse checkAdminRole(User user){
        if(user != null && user.getRole().intValue() == Const.Role.ROLE_ADMIN){//user非空，且Role值为1
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }



}
