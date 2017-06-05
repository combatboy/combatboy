/*------------------------------------------------------------
 // Copyright (C) 2015 正益无线（北京）科技有限公司  版权所有。
 // 文件名：
 // 文件功能描述：
 //
 // 创 建 人：陈恺垣
 // 创建日期：15/9/1
 //
 // 修 改 人：
 // 修改日期：
 // 修改描述：
 //-----------------------------------------------------------*/
var MESSAGE_WINDOW = null;
function MessageFrame(option) {
    this.option = $.extend({},this.defaultOption,option);
    MESSAGE_WINDOW = this;
}

MessageFrame.prototype.defaultOption = {
    name: 'messageFrame',
    url: 'messageFrame.html',
    popName: "",
    hasTitle:"1",
    title:"",
    titleColor:"#148af7",
    titleBackGround:"#f8f8f8",
    content1:"",
    content1Color:"#5e5d5d",
    content1BackGround:"#f8f8f8",
    content1TextAlign: '1',
    content2:"",
    content2Color:"#5e5d5d",
    content2BackGround:"#f8f8f8",
    content2TextAlign: '1',
    action:"1", // 1有1个按钮，2有2个按钮
    buttonConfirm:"确定", //
    buttonConfirmColor:"#fff", //
    buttonConfirmBackGround:"#148af7", //
    buttonCancel:"取消", //
    buttonCancelColor:"#fff", //
    buttonCancelBackGround:"#148af7", //
    buttonConfirmCb:function(result){alert(result)},
    buttonCancelCb:function(result){alert(result)},
    splitHeight:"1",
    splitColor:"#fff",
    splitBackGround:"148af7",
    bodyBackGround:"rgba(0,0,0,0.4)",
    contentBackGround:"#f8f8f8",
    borderRadius:"0.4em"
};

MessageFrame.prototype.open = function () {
    appcan.logs(JSON.stringify(this.option));
    appcan.locStorage.setVal("MessageFrameOption",JSON.stringify(this.option));
    appcan.frame.open({
        id: this.option.name,
        url: this.option.url,
        top: 0,
        left: 0
    });

    return this;
};
MessageFrame.prototype.confirm = function () {
    this.option.buttonConfirmCb(1);
    return this;
};
MessageFrame.prototype.cancel = function () {
    this.option.buttonCancelCb(0);
    return this;
};

MessageFrame.prototype.close = function () {
    MESSAGE_WINDOW = null;
};