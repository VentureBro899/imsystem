package venture.study.im.common.model;

/*
 * @author: Venture
 * @description: 请求公共基类
 */

import lombok.Data;

@Data
public class RequestBase {
    // 在多应用系统中，请求的应用id
    private Integer appId;
}
