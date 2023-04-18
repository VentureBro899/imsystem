package venture.study.im.service.friendship.controller;

/*
 * @author: Venture
 * @description: TODO
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import venture.study.im.common.ResponseVO;
import venture.study.im.service.friendship.model.req.ApproverFriendRequestReq;
import venture.study.im.service.friendship.model.req.GetFriendShipRequestReq;
import venture.study.im.service.friendship.model.req.ReadFriendShipRequestReq;
import venture.study.im.service.friendship.service.ImFriendShipRequestService;

@RestController
@RequestMapping("/v1/friendshipreq")
public class ImFriendShipReqController {
    @Autowired
    ImFriendShipRequestService imFriendShipRequestService;

    @RequestMapping("/approveFriendRequest")
    public ResponseVO approveFriendRequest(@RequestBody @Validated
                                           ApproverFriendRequestReq req, Integer appId, String identifier){
        req.setAppId(appId);
        req.setOperater(identifier);
        return imFriendShipRequestService.approverFriendRequest(req);
    }
    @RequestMapping("/getFriendRequest")
    public ResponseVO getFriendRequest(@RequestBody @Validated GetFriendShipRequestReq req, Integer appId){
        req.setAppId(appId);
        return imFriendShipRequestService.getFriendRequest(req.getFromId(),req.getAppId());
    }

    @RequestMapping("/readFriendShipRequestReq")
    public ResponseVO readFriendShipRequestReq(@RequestBody @Validated ReadFriendShipRequestReq req, Integer appId){
        req.setAppId(appId);
        return imFriendShipRequestService.readFriendShipRequestReq(req);
    }
}
