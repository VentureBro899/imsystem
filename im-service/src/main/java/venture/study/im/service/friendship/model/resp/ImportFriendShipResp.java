/*
 * @author: Venture
 * @description:
 */

/*
 * @author: Venture
 * @description:
 */

package venture.study.im.service.friendship.model.resp;

import lombok.Data;

import java.util.List;

/*
 * @author: Venture
 * @description: TODO
 */
@Data
public class ImportFriendShipResp {
    private List<String> successId;

    private List<String> errorId;
}
