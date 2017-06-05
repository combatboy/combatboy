/**
 * 下载服务
 * @param urlFrom {string} 需要下载的资源的url
 * @param urlTo {string} 保存路径
 * @param timeStamp {number}
 * @param callback {function}
 * @constructor
 */
function ClsDownloader(urlFrom, urlTo, timeStamp, callback){
    this._init();

    this._opId = this._genOpId();
    this.urlFromMd5 = appcan.crypto.md5(urlFrom);
    this.urlFrom = urlFrom;
    this.urlTo = urlTo || 'wgt://cttq_cache/cache_' + this.urlFromMd5;
    this.timeStamp = timeStamp;
    this.success = callback || ClsDownloader.noop;
    ClsDownloader.hashMap[this.urlFromMd5] = this._opId;

    ClsDownloader.cbSeqSuccess[this._opId] = {
        urlFrom: this.urlFrom,
        urlTo: this.urlTo,
        realPath: '',
        success: this.success,
        timeStamp: this.timeStamp,
        fileSize: 0
    };
    uexDownloaderMgr.createDownloader(this._opId);
}

//noop
ClsDownloader.noop = function () { };

//回调序列
ClsDownloader.cbSeqSuccess = {};
//
ClsDownloader.hashMap = {};

/**
 * 生成操作码
 * @returns {*}
 * @private
 */
ClsDownloader.prototype._genOpId = function () {
    var r;
    do {
        r = parseInt(Math.random().toString().substr(2, 6), 10);
    } while (r < 100000);
    return r;
};

//回调序列
ClsDownloader.cbSeqSuccess = {};

/**
 * 初始化插件：绑定回调
 * @private
 */
ClsDownloader.prototype._init = function () {

    if(!ClsDownloader.hasInit){
        uexDownloaderMgr.cbCreateDownloader = function(opId, dataType, data){
            if (dataType==2 && data==0) {
                //创建成功
                var cbObj = ClsDownloader.cbSeqSuccess[opId];
                //开始下载
                uexDownloaderMgr.download(opId, cbObj.urlFrom, cbObj.urlTo, 0);
            }
        };
        uexDownloaderMgr.onStatus = function(opId, fileSize, percent, status){
            if(status==1){
                //下载成功
                //关闭下载对象
                uexDownloaderMgr.closeDownloader(opId);

                //回调
                var cbObj = ClsDownloader.cbSeqSuccess[opId];
                cbObj.fileSize = fileSize;

                //cbObj.success.call(null, cbObj);
                uexFileMgr.getFileRealPath(cbObj.urlTo);
            }
        };

        //TODO: 如果其他地方也用到了此功能，应该注意处理覆盖问题
        uexFileMgr.cbGetFileRealPath = function(opId, dataType, data){
            if (dataType == 0) {
                var cbObjKey = ClsDownloader.hashMap[data.substr(data.length-32)];
                var cbObj = ClsDownloader.cbSeqSuccess[cbObjKey];
                if(cbObj){
                    cbObj.realPath = data;
                    cbObj.success.call(null, cbObj);
                }
            }
        };
        //初始化flag
        ClsDownloader.hasInit = true;
    }
};
