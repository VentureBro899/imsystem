/*
 * @author: Venture
 * @description:
 */

package venture.study.im.service.friendship.service.imp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import venture.study.im.common.ResponseVO;
import venture.study.im.common.enums.CheckFriendShipTypeEnum;
import venture.study.im.common.enums.FriendShipErrorCode;
import venture.study.im.common.enums.FriendShipStatusEnum;
import venture.study.im.common.exception.ApplicationException;
import venture.study.im.common.model.RequestBase;
import venture.study.im.service.friendship.dao.ImFriendShipEntity;
import venture.study.im.service.friendship.dao.mapper.ImFriendShipMapper;
import venture.study.im.service.friendship.model.req.*;
import venture.study.im.service.friendship.model.resp.CheckFriendShipResp;
import venture.study.im.service.friendship.model.resp.ImportFriendShipResp;
import venture.study.im.service.friendship.service.ImFriendShipService;
import venture.study.im.service.user.dao.ImUserDataEntity;
import venture.study.im.service.user.service.ImUserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/*
 * @author: Venture
 * @description: TODO
 */

@Service
public class ImFriendShipServiceImp implements ImFriendShipService {
    @Autowired
    ImFriendShipMapper imFriendShipMapper;

    @Autowired
    ImUserService imUserService;

    @Override
    public ResponseVO importFriendShip(ImporFriendShipReq req) {

        if(req.getFriendItem().size() > 100){
            return ResponseVO.errorResponse(FriendShipErrorCode.IMPORT_SIZE_BEYOND);
        }
        ImportFriendShipResp resp = new ImportFriendShipResp();
        List<String> successId = new ArrayList<>();
        List<String> errorId = new ArrayList<>();

        for (ImporFriendShipReq.ImportFriendDto dto:
                req.getFriendItem()) { // 一个用户有多个好友
            ImFriendShipEntity entity = new ImFriendShipEntity();
            BeanUtils.copyProperties(dto,entity);
            // 注意插入appId与FromId
            entity.setAppId(req.getAppId());
            entity.setFromId(req.getFromId());
            try {
                int insert = imFriendShipMapper.insert(entity);
                if(insert == 1){
                    successId.add(dto.getToId());
                }else{
                    errorId.add(dto.getToId());
                }
            }catch (Exception e){
                e.printStackTrace();
                errorId.add(dto.getToId());
            }

        }

        resp.setErrorId(errorId);
        resp.setSuccessId(successId);

        return ResponseVO.successResponse(resp);
    }

    @Override
    public ResponseVO addFriend(AddFriendReq req) {
        ResponseVO<ImUserDataEntity> fromInfo = imUserService.getSingleUserInfo(req.getFromId(), req.getAppId());
        if(!fromInfo.isOk()){
            return fromInfo;
        }

        ResponseVO<ImUserDataEntity> toInfo = imUserService.getSingleUserInfo(req.getToItem().getToId(), req.getAppId());
        if(!toInfo.isOk()){
            return toInfo;
        }

        /*ImUserDataEntity data = toInfo.getData();

        if(data.getFriendAllowType() != null && data.getFriendAllowType() == AllowFriendTypeEnum.NOT_NEED.getCode()){
            return this.doAddFriend(req,req.getFromId(), req.getToItem(), req.getAppId());
        }else{
            QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
            query.eq("app_id",req.getAppId());
            query.eq("from_id",req.getFromId());
            query.eq("to_id",req.getToItem().getToId());
            ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(query);
            if(fromItem == null || fromItem.getStatus()
                    != FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode()){
                //插入一条好友申请的数据
                ResponseVO responseVO = imFriendShipRequestService.addFienshipRequest(req.getFromId(), req.getToItem(), req.getAppId());
                if(!responseVO.isOk()){
                    return responseVO;
                }
            }else{
                return ResponseVO.errorResponse(FriendShipErrorCode.TO_IS_YOUR_FRIEND);
            }

        }*/

        return doAddFriend(req, req.getFromId(), req.getToItem(),req.getAppId());

    }

    @Override
    @Transactional
    public ResponseVO doAddFriend(RequestBase requestBase, String fromId, FriendDto dto, Integer appId){

        //A添加B
        //Friend表插入A 和 B 两条记录，即A + B 与 B + A
        //查询是否有记录存在，如果存在则判断状态，如果是已添加，则提示已添加，如果是未添加，则修改状态

        // A + B
        QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
        query.eq("app_id",appId);
        query.eq("from_id",fromId);
        query.eq("to_id",dto.getToId());
        ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(query);
        if(fromItem == null){  // 不存在记录，走添加逻辑。
            fromItem = new ImFriendShipEntity();
            fromItem.setAppId(appId);
            fromItem.setFromId(fromId);
            BeanUtils.copyProperties(dto,fromItem);
            fromItem.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
            fromItem.setCreateTime(System.currentTimeMillis());
            int insert = imFriendShipMapper.insert(fromItem);
            if(insert != 1){
                return ResponseVO.errorResponse(FriendShipErrorCode.ADD_FRIEND_ERROR);
            }
        } else{
            //如果存在则判断状态，如果是已添加，则提示已添加，如果是已删除，则修改状态
            if(fromItem.getStatus() == FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode()){
                return ResponseVO.errorResponse(FriendShipErrorCode.TO_IS_YOUR_FRIEND);
            } else{
                ImFriendShipEntity entity = new ImFriendShipEntity();

                if(StringUtils.isNotBlank(dto.getAddSource())){
                    entity.setAddSource(dto.getAddSource());
                }

                if(StringUtils.isNotBlank(dto.getRemark())){
                    entity.setRemark(dto.getRemark());
                }

                if(StringUtils.isNotBlank(dto.getExtra())){
                    entity.setExtra(dto.getExtra());
                }
                entity.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());

                int result = imFriendShipMapper.update(entity, query);
                if(result != 1){
                    return ResponseVO.errorResponse(FriendShipErrorCode.ADD_FRIEND_ERROR);
                }
            }

        }


        // B + A
       /* QueryWrapper<ImFriendShipEntity> toQuery = new QueryWrapper<>();
        toQuery.eq("app_id",appId);
        toQuery.eq("from_id",dto.getToId());
        toQuery.eq("to_id",fromId);
        ImFriendShipEntity toItem = imFriendShipMapper.selectOne(toQuery);
        if(toItem == null){ // 记录不存在
            toItem = new ImFriendShipEntity();
            toItem.setAppId(appId);
            toItem.setFromId(dto.getToId());
            BeanUtils.copyProperties(dto,toItem);
            toItem.setToId(fromId);
            toItem.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
            toItem.setCreateTime(System.currentTimeMillis());
//            toItem.setBlack(FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode());
            int insert = imFriendShipMapper.insert(toItem);
        }else{
            if(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode() !=
                    toItem.getStatus()){
                ImFriendShipEntity update = new ImFriendShipEntity();
                update.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
                imFriendShipMapper.update(update,toQuery);
            }
        }*/
        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO updateFriend(UpdateFriendReq req) {
        // 先查询两个用户是否存在
        ResponseVO<ImUserDataEntity> fromInfo = imUserService.getSingleUserInfo(req.getFromId(), req.getAppId());
        if(!fromInfo.isOk()){
            return fromInfo;
        }

        ResponseVO<ImUserDataEntity> toInfo = imUserService.getSingleUserInfo(req.getToItem().getToId(), req.getAppId());
        if(!toInfo.isOk()){
            return toInfo;
        }

        ResponseVO responseVO = this.doUpdate(req.getFromId(), req.getToItem(), req.getAppId());
        return responseVO;
    }

    @Override
    public ResponseVO deleteFriend(DeleteFriendReq req) {
        // 首先查询两个用户是不是好友关系，必须是好友才能删除
        LambdaQueryWrapper<ImFriendShipEntity> query = new LambdaQueryWrapper<>();
        query.eq(ImFriendShipEntity::getAppId,req.getAppId());
        query.eq(ImFriendShipEntity::getFromId,req.getFromId());
        query.eq(ImFriendShipEntity::getToId,req.getToId());
        ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(query);
        if(fromItem == null){ // 不存在此好友关系，无法删除
            return ResponseVO.errorResponse(FriendShipErrorCode.TO_IS_NOT_YOUR_FRIEND);
        }else{
            if(fromItem.getStatus() == FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode()){ // 是好友关系 执行删除操作
                LambdaUpdateWrapper<ImFriendShipEntity> luw = new LambdaUpdateWrapper<>();
                luw.set(ImFriendShipEntity::getStatus,FriendShipStatusEnum.FRIEND_STATUS_DELETE).
                        eq(ImFriendShipEntity::getFromId,req.getFromId()).
                        eq(ImFriendShipEntity::getToId,req.getToId()).
                        eq(ImFriendShipEntity::getAppId,req.getAppId());
                int update = imFriendShipMapper.update(null, luw);
            }else{ // 先前已删除
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_DELETED);
            }
        }
        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO deleteAllFriend(DeleteFriendReq req) {
        QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
        query.eq("app_id",req.getAppId());
        query.eq("from_id",req.getFromId());
        query.eq("status",FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());

        ImFriendShipEntity update = new ImFriendShipEntity();
        update.setStatus(FriendShipStatusEnum.FRIEND_STATUS_DELETE.getCode());
        imFriendShipMapper.update(update,query);
        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO getAllFriendShip(GetAllFriendShipReq req) {
        LambdaQueryWrapper<ImFriendShipEntity> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ImFriendShipEntity::getAppId,req.getAppId())
                .eq(ImFriendShipEntity::getFromId,req.getFromId());
        List<ImFriendShipEntity> friends = imFriendShipMapper.selectList(lqw);
        return ResponseVO.successResponse(friends);
    }

    @Override
    public ResponseVO getRelation(GetFriendShipReq req) {
        LambdaQueryWrapper<ImFriendShipEntity> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ImFriendShipEntity::getAppId,req.getAppId());
        lqw.eq(ImFriendShipEntity::getFromId,req.getFromId());
        lqw.eq(ImFriendShipEntity::getToId,req.getToId());

        ImFriendShipEntity entity = imFriendShipMapper.selectOne(lqw);
        if(entity == null){
            return ResponseVO.errorResponse(FriendShipErrorCode.REPEATSHIP_IS_NOT_EXIST);
        }
        return ResponseVO.successResponse(entity);
    }

    @Override
    public ResponseVO checkFriendship(CheckFriendShipReq req) {
        Map<String, Integer> result
                = req.getToIds().stream()
                .collect(Collectors.toMap(Function.identity(), s -> 0));

        List<CheckFriendShipResp> resp = new ArrayList<>();

        if(req.getCheckType() == CheckFriendShipTypeEnum.SINGLE.getType()){ //单向校验
            resp =imFriendShipMapper.checkFriendShip(req);
        }else { // 双向校验
            resp =imFriendShipMapper.checkFriendShipBoth(req);
        }

        Map<String, Integer> collect = resp.stream()
                .collect(Collectors.toMap(CheckFriendShipResp::getToId
                        , CheckFriendShipResp::getStatus));

        for (String toId : result.keySet()){
            if(!collect.containsKey(toId)){ // 将好友列表中没有的关系标记为0
                CheckFriendShipResp checkFriendShipResp = new CheckFriendShipResp();
                checkFriendShipResp.setFromId(req.getFromId());
                checkFriendShipResp.setToId(toId);
                checkFriendShipResp.setStatus(result.get(toId));
                resp.add(checkFriendShipResp);
            }
        }

        return ResponseVO.successResponse(resp);
    }

    @Override
    public ResponseVO addBlack(AddFriendShipBlackReq req) {
        // 校验两个用户是否存在
        ResponseVO<ImUserDataEntity> fromInfo = imUserService.getSingleUserInfo(req.getFromId(), req.getAppId());
        if(!fromInfo.isOk()){
            return fromInfo;
        }

        ResponseVO<ImUserDataEntity> toInfo = imUserService.getSingleUserInfo(req.getToId(), req.getAppId());
        if(!toInfo.isOk()){
            return toInfo;
        }


        QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
        query.eq("app_id",req.getAppId());
        query.eq("from_id",req.getFromId());
        query.eq("to_id",req.getToId());

        ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(query);
        if(fromItem == null){ // 关系不存在
            //走添加关系逻辑。
            fromItem = new ImFriendShipEntity();
            fromItem.setFromId(req.getFromId());
            fromItem.setToId(req.getToId());
            fromItem.setAppId(req.getAppId());
            // 设置为拉黑
            fromItem.setBlack(FriendShipStatusEnum.BLACK_STATUS_BLACKED.getCode());
            fromItem.setCreateTime(System.currentTimeMillis());
            int insert = imFriendShipMapper.insert(fromItem);
            if(insert != 1){
                return ResponseVO.errorResponse(FriendShipErrorCode.ADD_FRIEND_ERROR);
            }

        } else{
            //如果存在则判断状态，如果是拉黑，则提示已拉黑，如果是未拉黑，则修改状态
            if(fromItem.getBlack() != null && fromItem.getBlack() == FriendShipStatusEnum.BLACK_STATUS_BLACKED.getCode()){
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_BLACK);
            } else {
                ImFriendShipEntity update = new ImFriendShipEntity();
                update.setBlack(FriendShipStatusEnum.BLACK_STATUS_BLACKED.getCode());
                int result = imFriendShipMapper.update(update, query);
                if(result != 1){
                    return ResponseVO.errorResponse(FriendShipErrorCode.ADD_BLACK_ERROR);
                }
            }
        }

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO deleteBlack(DeleteBlackReq req) {
        QueryWrapper queryFrom = new QueryWrapper<>()
                .eq("from_id", req.getFromId())
                .eq("app_id", req.getAppId())
                .eq("to_id", req.getToId());
        ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(queryFrom);
        if (fromItem == null){ // 不存在关系链记录
            throw new ApplicationException(FriendShipErrorCode.REPEATSHIP_IS_NOT_EXIST);
        }
        if (fromItem.getBlack() != null && fromItem.getBlack() == FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode()) {
            throw new ApplicationException(FriendShipErrorCode.FRIEND_IS_NOT_YOUR_BLACK);
        }

        ImFriendShipEntity update = new ImFriendShipEntity();
        update.setBlack(FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode());
        int update1 = imFriendShipMapper.update(update, queryFrom);
        if(update1 == 1){
            return ResponseVO.successResponse();
        }
        return ResponseVO.errorResponse();
    }

    @Override
    public ResponseVO checkBlack(CheckFriendShipReq req) {
        Map<String, Integer> toIdMap
                = req.getToIds().stream().collect(Collectors
                .toMap(Function.identity(), s -> 0));
        List<CheckFriendShipResp> result = new ArrayList<>();
        if (req.getCheckType() == CheckFriendShipTypeEnum.SINGLE.getType()) {
            result = imFriendShipMapper.checkFriendShipBlack(req);
        } else {
            result = imFriendShipMapper.checkFriendShipBlackBoth(req);
        }

        Map<String, Integer> collect = result.stream()
                .collect(Collectors
                        .toMap(CheckFriendShipResp::getToId,
                                CheckFriendShipResp::getStatus));
        for (String toId:
                toIdMap.keySet()) {
            if(!collect.containsKey(toId)){
                CheckFriendShipResp checkFriendShipResp = new CheckFriendShipResp();
                checkFriendShipResp.setToId(toId);
                checkFriendShipResp.setFromId(req.getFromId());
                checkFriendShipResp.setStatus(toIdMap.get(toId));
                result.add(checkFriendShipResp);
            }
        }

        return ResponseVO.successResponse(result);
    }

    @Transactional
    public ResponseVO doUpdate(String fromId, FriendDto dto,Integer appId){
        // 更新来源，备注，签名
        LambdaUpdateWrapper<ImFriendShipEntity> luw = new LambdaUpdateWrapper<>();
        luw.set(ImFriendShipEntity::getAddSource,dto.getAddSource())
                .set(ImFriendShipEntity::getExtra,dto.getExtra())
                .set(ImFriendShipEntity::getRemark,dto.getRemark())
                .eq(ImFriendShipEntity::getAppId,appId)
                .eq(ImFriendShipEntity::getToId,dto.getToId())
                .eq(ImFriendShipEntity::getFromId,fromId);

        int update = imFriendShipMapper.update(null, luw);
        if(update == 1){
            return ResponseVO.successResponse();
        }

        return ResponseVO.errorResponse();
    }
}
