package venture.study.im.service.friendship.service;


import venture.study.im.common.ResponseVO;
import venture.study.im.service.friendship.model.req.ApproverFriendRequestReq;
import venture.study.im.service.friendship.model.req.FriendDto;
import venture.study.im.service.friendship.model.req.ReadFriendShipRequestReq;

public interface ImFriendShipRequestService {

    public ResponseVO addFrienshipRequest(String fromId, FriendDto dto, Integer appId);

    public ResponseVO approverFriendRequest(ApproverFriendRequestReq req);

    public ResponseVO readFriendShipRequestReq(ReadFriendShipRequestReq req);

    public ResponseVO getFriendRequest(String fromId, Integer appId);
}
