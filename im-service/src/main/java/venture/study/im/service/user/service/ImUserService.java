

package venture.study.im.service.user.service;

import venture.study.im.common.ResponseVO;
import venture.study.im.service.user.dao.ImUserDataEntity;
import venture.study.im.service.user.model.req.*;
import venture.study.im.service.user.model.resp.GetUserInfoResp;

/*
 * @author: Venture
 * @description:
 */

public interface ImUserService {
    // 用户数据导入
    public ResponseVO  importUser(ImportUserReq req);

    public ResponseVO<ImUserDataEntity> getSingleUserInfo(String userId, Integer appId);

    public ResponseVO<GetUserInfoResp> getUserInfo(GetUserInfoReq req);

    public ResponseVO deleteUser(DeleteUserReq req);

    public ResponseVO modifyUserInfo(ModifyUserInfoReq req);
}
