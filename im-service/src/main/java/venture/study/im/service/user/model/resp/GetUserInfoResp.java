package venture.study.im.service.user.model.resp;


import lombok.Data;
import venture.study.im.service.user.dao.ImUserDataEntity;

import java.util.List;


@Data
public class GetUserInfoResp {

    private List<ImUserDataEntity> userDataItems;

    private List<String> failUserIds;


}
