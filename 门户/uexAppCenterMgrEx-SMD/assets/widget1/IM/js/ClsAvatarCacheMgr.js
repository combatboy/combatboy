/**
 * 头像缓存管理
 * @constructor
 */
function AvatarCacheMgr(){

}

/**
 * 渲染头像：
 * img元素 =》 src
 * 其他元素 =》 background-image
 * @param elm
 * @param realPath
 */
AvatarCacheMgr.renderAvatar = function (elm, realPath) {
    var isImg = elm.nodeName.toUpperCase() == 'IMG';
    if(isImg){
        elm.src = realPath;
    }else{
        elm.style.backgroundImage = 'url(' + realPath + ')';
    }
};

/**
 * 下载图片
 * @param elm
 * @param hecate
 * @param hecateUrl
 * @param hecateTime
 */
AvatarCacheMgr.downloadAdapter = function (elm, hecate, hecateUrl, hecateTime) {

    new ClsDownloader(hecateUrl, '', parseInt(hecateTime), function (imgInfo) {
        //download ok, render
        AvatarCacheMgr.renderAvatar(elm, imgInfo.realPath);

        //WebSQL insert
        ClsTableAdapter.getTable('cttq_cache_multi').c(function () {
            //ok
        }, function () {
            //err
            console.error('Fail to execute WebSQL insert');
        }, {
            'hecate': hecate,
            'url_from': imgInfo.urlFrom,
            'url_to': imgInfo.urlTo,
            'real_path': imgInfo.realPath,
            'time': imgInfo.timeStamp,
            'size': imgInfo.fileSize
        });
    });
};

AvatarCacheMgr._inprog = '.hecate-inprog';

/**
 * 同步
 * 主调函数
 */
AvatarCacheMgr.sync = function () {
    var selector = '[hecate]:not(' + AvatarCacheMgr._inprog + ')';
    var $elms = $(selector);

    var hecateStack = [];

    $elms.each(function (index, elm) {
        var hecateUrl = elm.getAttribute('hecate-url') || '',
            hecateTime = elm.getAttribute('hecate-time') || '';

        //mark dom node
        //push stack
        if(!hecateUrl){
            console.log('url is empty');
        }else if(!hecateTime){
            console.error('time is required');
        }else{
            var fullUrl = hecateUrl + '?t=' + hecateTime,
                hecate = appcan.crypto.md5(fullUrl);

            elm.setAttribute('hecate', hecate);
            $(elm).addClass(AvatarCacheMgr._inprog.substr(1));

            //push stack for iterate
            hecateStack.push(hecate);
        }
    });

    if(hecateStack.length){
        //WebSQL query
        ClsTableAdapter.getTable('cttq_cache_multi').r(function (data) {
            AvatarCacheMgr.cacheCompare(data, $elms);
        }, hecateStack, 'in', 'hecate');
    }
};

/**
 *
 * @param data
 * @param $elms
 */
AvatarCacheMgr.cacheCompare = function (data, $elms){
    var res;
    try{
        res = JSON.parse(data);
    }catch(ex){
        res = [];
    }
    if(!(res instanceof Array)){
        throw new Error('Select res is not an array');
    } else {
        var tblRes = {};

        //render cached avatar, whatever
        res.forEach(function (o) {
            var nod = document.querySelector('[hecate="' + o.hecate+ '"]');
            //cache hit, render
            AvatarCacheMgr.renderAvatar(nod, o.real_path);
            //speed up
            tblRes[o.hecate] = o;
        });

        //iterate attached elements
        $elms.each(function (index, elm) {
            var hecateUrl = elm.getAttribute('hecate-url') || '',
                hecateTime = elm.getAttribute('hecate-time') || '',
                hecate = elm.getAttribute('hecate') || '';

            var tblRow = tblRes[hecate];

            if(!tblRow){
                //miss
                AvatarCacheMgr.downloadAdapter(elm, hecate, hecateUrl, hecateTime);
                $(document).triggerHandler('AvatarCacheMgr.cacheMiss', [hecate, hecateUrl]);
            }else if(tblRow.time != hecateTime){
                //dirty
                AvatarCacheMgr.downloadAdapter(elm, hecate, hecateUrl, hecateTime);
            }else{
                //todo: 需要封装
                var $doc = $(document),
                    opId = QNS.util.genOpId();
                uexFileMgr.cbIsFileExistByPath = function (opId, dataType, data) {
                    $doc.triggerHandler('uexFileMgr.cbIsFileExistByPath', [{
                        opId: opId,
                        dataType: dataType,
                        data: data
                    }]);
                };
                uexFileMgr.isFileExistByPath(opId, tblRow.real_path);
                $doc.on('uexFileMgr.cbIsFileExistByPath', function (e, o) {
                    if(o.opId == opId){
                        if(o.data==0){
                            AvatarCacheMgr.renderAvatar(elm, hecateUrl);
                            ClsTableAdapter.getTable('cttq_cache_multi').d(function () {
                                //success
                                qdebug('FILE MISS!!! delete row from db ok: ' + hecateUrl);
                            }, function () {
                                //error
                                qdebug('FILE MISS!!! delete row from db fail: ' + hecateUrl);
                            }, {
                                'hecate': hecate
                            });
                        }
                    }
                });
                //finish
            }
        });
    }
};