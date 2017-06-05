/**
 * Created by mitty on 15/11/28.
 */
/**
 * 滚动管理
 * @param {string} selector css选择器
 */
function OrgScrollMgr(selector){
    //this._$target = undefined;
    //this._offsetTop = undefined;
    //this._scrollTop = undefined;
    this._$g = $(selector);
}
OrgScrollMgr.prototype.setTarget = function (elm) {
    this._$target = $(elm);
    this._markPosition();
};
OrgScrollMgr.prototype._markPosition = function () {
    this._offsetTop = this._$target.offset().top;
    this._scrollTop = $(window).scrollTop();
//        console.log('_offsetTop: %s, _scrollTop', this._offsetTop, this._scrollTop);
};
OrgScrollMgr.prototype.scrollIt = function () {
    if(this._$target && this._offsetTop!==undefined){
        var rectTop = this._$target.offset().top,
                a = rectTop - (this._offsetTop - this._scrollTop);
        $('html,body').scrollTop(a);
//            console.log('rectTop: %s', rectTop);
    }
};