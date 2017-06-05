/**
 * 子应用同步助手
 * @param cb {function} 列表处理成功后的回调
 */
function preInstallAppsHandler(cb) {
    //ios系统中app被删除时，子应用也会被同时删除
    if($.os.ios){
        console.warn('ios不需要对子应用进行特殊处理');
        return;
    }

    //deep copy
    var apps = SimcereConfig.preInstallApps.concat([]);

    function listItemHandler() {
        var appId = apps.pop();

        //可以选用解压、删除中的其中一种方案
        preInstallAppsHandler.del(appId, function (data) {
            if (data != 0) {
                console.log('子应用任务项处理失败： ' + appId);
                //处理失败也要继续，失败的原因一般是该子应用未安装，对应路径不存在
                listItemHandler();
            } else if (!apps.length) {
                console.log('子应用任务列表处理完成');
                //列表处理完成，执行回调通知，
                //此处存在的问题是，如果是意外的删除失败，可能导致之后滞留旧的包，
                //直到下次更新才会被重新尝试处理
                cb();
            } else {
                //还有未处理的
                listItemHandler();
            }
        });
    }

    //开始处理流程
    listItemHandler();
}

/**
 * 解压处理
 * @param appId
 * @param cb
 */
preInstallAppsHandler.unzip = function (appId, cb) {
    setTimeout(function () {
        //回调
        uexZip.cbUnZip = function (opId, dataType, data) {
            console.log('uexZip.cbUnZip ' + appId + ': ' + data);
            //执行回调
            cb && cb(data);
        };
        //run
        console.log('uexZip.unzip: ' + appId);
        uexZip.unzip('res://' + appId + '.zip', 'wgts://');
    }, 0);
};

/**
 * 删除处理
 * @param appId
 * @param cb
 */
preInstallAppsHandler.del = function (appId, cb) {
    setTimeout(function () {
        uexFileMgr.cbDeleteFileByPath = function (opId, dataType, data) {
            console.log('uexFileMgr.cbDeleteFileByPath ' + appId + ': ' + data);
            //执行回调
            cb && cb(data);
        };
        //run
        console.log('uexFileMgr.deleteFileByPath: ' + appId);
        uexFileMgr.deleteFileByPath('wgts://' + appId);
    }, 0);
};
