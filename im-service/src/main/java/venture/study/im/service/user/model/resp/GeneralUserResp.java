

package venture.study.im.service.user.model.resp;

import lombok.Data;


import java.util.List;


/*
 * @author: Venture
 * @description: 通用返回
 */
@Data
public class GeneralUserResp {
    private List<String> successItems;
    private List<String> failsItems;
}
