package venture.study.im.common.enums;

/*
 * @author: Venture
 * @description: TODO
 */

public enum CheckFriendShipTypeEnum {

    /**
     * 1 单方校验；2双方校验。
     */
    SINGLE(1),

    BOTH(2),
    ;

    private int type;

    CheckFriendShipTypeEnum(int type){
        this.type=type;
    }

    public int getType() {
        return type;
    }
}
