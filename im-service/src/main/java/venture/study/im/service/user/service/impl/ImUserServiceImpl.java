/*
 * @author: Venture
 * @description:
 */

package venture.study.im.service.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import venture.study.im.common.ResponseVO;
import venture.study.im.common.enums.DelFlagEnum;
import venture.study.im.common.enums.UserErrorCode;
import venture.study.im.common.exception.ApplicationException;
import venture.study.im.service.user.dao.ImUserDataEntity;
import venture.study.im.service.user.dao.mapper.ImUserDataMapper;
import venture.study.im.service.user.model.req.DeleteUserReq;
import venture.study.im.service.user.model.req.GetUserInfoReq;
import venture.study.im.service.user.model.req.ImportUserReq;
import venture.study.im.service.user.model.req.ModifyUserInfoReq;
import venture.study.im.service.user.model.resp.GetUserInfoResp;
import venture.study.im.service.user.model.resp.GeneralUserResp;
import venture.study.im.service.user.service.ImUserService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


@Service
public class ImUserServiceImpl implements ImUserService {
    @Autowired
    ImUserDataMapper imUserDataMapper;


    // 批量导入用户信息
    @Override
    public ResponseVO importUser(ImportUserReq req) {
        if (req.getUserList().size() > 100){ // 一次性不能插入过多
            // todo 用户信息过多
        }

        ArrayList<String> successID = new ArrayList<>();
        ArrayList<String> errorID = new ArrayList<>();

        req.getUserList().forEach(u -> {
            u.setAppId(req.getAppId());
            try {
                int flag = imUserDataMapper.insert(u);
                if (flag > 0){
                    successID.add(u.getUserId());
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorID.add(u.getUserId());
            }
        });

        GeneralUserResp importUserResp = new GeneralUserResp();
        importUserResp.setSuccessItems(successID);
        importUserResp.setFailsItems(errorID);

        return ResponseVO.successResponse(importUserResp);
    }

    // 查询单个
    @Override
    public ResponseVO<ImUserDataEntity> getSingleUserInfo(String userId, Integer appId) {
        LambdaQueryWrapper<ImUserDataEntity> lbq = new LambdaQueryWrapper<>();
        lbq.eq(ImUserDataEntity::getUserId,userId).eq(ImUserDataEntity::getAppId,appId);
        ImUserDataEntity imUserDataEntity = imUserDataMapper.selectOne(lbq);
        if(imUserDataEntity == null){
            return ResponseVO.errorResponse(UserErrorCode.USER_IS_NOT_EXIST);
        }

        return ResponseVO.successResponse(imUserDataEntity);
    }


    // 批量查询
    @Override
    public ResponseVO<GetUserInfoResp> getUserInfo(GetUserInfoReq req) {
        if(req.getUserIds().size() > 100){
            // todo 防止一次性查询过多
        }

        ArrayList<String> failItems = new ArrayList<>();

        LambdaQueryWrapper<ImUserDataEntity> lbq = new LambdaQueryWrapper<>();
        lbq.in(ImUserDataEntity::getUserId,req.getUserIds()).eq(ImUserDataEntity::getDelFlag,DelFlagEnum.NORMAL.getCode());
        List<ImUserDataEntity> successItems = imUserDataMapper.selectList(lbq);

        HashSet<String> set = new HashSet<>();// 已经查询到的数组
        successItems.forEach(item -> {
            set.add(item.getUserId());
        });
        // 用所有待查询的数组减去已查询到的数组得出查询失败的数组
        req.getUserIds().forEach(item -> {
            if(!set.contains(item)){
                failItems.add(item);
            }
        });
        GetUserInfoResp getUserInfoResp = new GetUserInfoResp();
        getUserInfoResp.setUserDataItems(successItems);
        getUserInfoResp.setFailUserIds(failItems);
        return ResponseVO.successResponse(getUserInfoResp);
    }

    // 删除用户信息，支持批量删除
    @Override
    public ResponseVO deleteUser(DeleteUserReq req) {
        // 构建对象只设置删除标记的对象，用于更新逻辑删除标记
        ImUserDataEntity entity = new ImUserDataEntity();
        entity.setDelFlag(DelFlagEnum.DELETE.getCode());

        ArrayList<String> successItems = new ArrayList<>();
        ArrayList<String> failItems = new ArrayList<>();


        req.getUserIds().forEach(item -> {
            LambdaUpdateWrapper<ImUserDataEntity> luw = new LambdaUpdateWrapper<>();
            luw.eq(ImUserDataEntity::getDelFlag, DelFlagEnum.NORMAL.getCode())
                   // .set(ImUserDataEntity::getDelFlag,DelFlagEnum.DELETE.getCode()) 因为前边的对象是set了删除标记，故而这里无需set
                    .eq(ImUserDataEntity::getUserId,item);
            try {
                int flag = imUserDataMapper.update(entity,luw);
                if(flag > 0){
                    successItems.add(item);
                }else {
                    failItems.add(item);
                }
            } catch (Exception e) {
                e.printStackTrace();
                failItems.add(item);
            }
        });

        GeneralUserResp generalUserResp = new GeneralUserResp();
        generalUserResp.setSuccessItems(successItems);
        generalUserResp.setFailsItems(failItems);

        return ResponseVO.successResponse(generalUserResp);
    }

    // 修改用户信息
    @Override
    public ResponseVO modifyUserInfo(ModifyUserInfoReq req) {
        LambdaUpdateWrapper<ImUserDataEntity> luw = new LambdaUpdateWrapper<>();
        luw.eq(ImUserDataEntity::getUserId,req.getUserId()).
                eq(ImUserDataEntity::getAppId,req.getAppId()).
                eq(ImUserDataEntity::getDelFlag,DelFlagEnum.NORMAL.getCode()).
                set(ImUserDataEntity::getNickName,req.getNickName()).
                set(ImUserDataEntity::getLocation,req.getLocation()).
                set(ImUserDataEntity::getBirthDay,req.getBirthDay()).
                set(ImUserDataEntity::getPassword,req.getPassword()).
                set(ImUserDataEntity::getPhoto,req.getPhoto()).
                set(ImUserDataEntity::getUserSex,req.getUserSex()).
                set(ImUserDataEntity::getSelfSignature,req.getSelfSignature()).
                set(ImUserDataEntity::getFriendAllowType,req.getFriendAllowType()).
                set(ImUserDataEntity::getExtra,req.getExtra());
        LambdaQueryWrapper<ImUserDataEntity> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ImUserDataEntity::getUserId,req.getUserId());
        ImUserDataEntity user = imUserDataMapper.selectOne(lqw);
        if(user == null){ // 为空直接抛异常
            throw new ApplicationException(UserErrorCode.USER_IS_NOT_EXIST);
        }
        int flag = imUserDataMapper.update(user, luw);
        if(flag > 0){
            return ResponseVO.successResponse();
        }
        throw new ApplicationException(UserErrorCode.MODIFY_USER_ERROR);
    }
}
