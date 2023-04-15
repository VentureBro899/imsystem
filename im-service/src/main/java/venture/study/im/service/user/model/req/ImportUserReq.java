/*
 * @author: Venture
 * @description:
 */

package venture.study.im.service.user.model.req;

import lombok.Data;
import venture.study.im.common.model.RequestBase;
import venture.study.im.service.user.dao.ImUserDataEntity;

import java.util.List;

@Data
public class ImportUserReq extends RequestBase {
    private List<ImUserDataEntity> userList;
}
