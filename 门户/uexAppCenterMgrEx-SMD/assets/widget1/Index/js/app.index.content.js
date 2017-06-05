/**
 * Created by mitty on 15/11/30.
 */
appcan.ready(function() {
    getGratefulList();
    //apiTest.init.call();
    uexWidget.cbStartWidget = function() {
        console.log('uexWidget.cbStartWidget');
    };

    angular.bootstrap(document.documentElement, ['zkApp']);
    appcan.initBounce();
    //启动子应用磁贴插件
    initChildAppHandler.call();

    //通过推送，打开神经网络子应用
    appcan.window.subscribe("root.openNNByPush", function() {
        uexAppCenterMgrEx.isAppInstalled(SimcereConfig.widgets.NN);
    });
    //通过推送，打开客户管理子应用
    appcan.window.subscribe("root.openKHGLByPush", function() {
        //alert("打开客户管理子应用");
        uexAppCenterMgrEx.isAppInstalled(SimcereConfig.widgets.KHGL);
    });

    //子应用同步机制
    syncWgtsHelper();
});

//获取感恩消息列表
function getGratefulList() {
    var URL = SimcereConfig.server.mas + '/ganen/ScrGetGanenList';
    var params = {
        pageNumber : 1,
        pageSize : 20
    };
    appcan.window.openToast("正在加载...");
    appcan.request.ajax({
        type : 'POST',
        url : URL,
        data : params,
        contentType : 'application/json',
        dataType : 'json',
        success : function(data) {
            appcan.window.closeToast();
            if (data.status == 0) {
                console.log(data);
                var result = {
                    dataList : data
                }
                var tabViewTmp = appcan.view.template($("#tpl_gratefulList").html());
                var html = tabViewTmp(result);
                $("#gratefulList").html(html);
            } else {
                appcan.window.openToast(data.msg, 1500, 5, 0);
            }
        },
        error : function() {
            //todo:errorHandler
            appcan.window.openToast('网络错误，请重试', 2000);
        }
    });
}


angular.module('zkApp', ['ngUtilSimcere']).service('getNNList', function() {
    return function() {
        var scope = angular.element(document.querySelector('.n_NNList')).scope();

        var params = {
            "userId" : localStorage.getItem('simcere.runtime.userId'),
            "pageNumber" : 1,
            "pageSize" : 2,
            "rangeId" : "6"
        };
        console.log(params);
        appcan.window.openToast('正在加载');
        appcan.request.ajax({
            type : 'POST',
            url : SimcereConfig.server.mas + '/message/ScrQueryMessages',
            data : params,
            contentType : 'application/json',
            dataType : 'json',
            success : function(data, status, xhr) {
                appcan.window.closeToast();
                console.log(data);
                if (data.status != '0') {
                    //todo:err
                } else if (data.message instanceof Array) {
                    scope.$apply(function() {
                        scope.list = data.message;
                    });
                }
            },
            error : function(xhr, errorType, error) {
                appcan.window.openToast('网络连接不可用', 2000);
            }
        });
    };
}).filter('formatTimeFromBBS', function() {
    var ite = function(o) {
        return o.length > 1 ? o : '0' + o;
    };
    return function(dateStr) {
        var dt = dateStr.split(' ');
        return dt[0].split('-').map(ite).join('-') + ' ' + dt[1];
    };
}).service('openBBS', function() {
    return function(tid, fid) {
        //设置标记，用于直接打开bbs的最新帖子页面
        localStorage.setItem('simcere.runtime.openLatestPostList', '1');
        uexAppCenterMgrEx.isAppInstalled(SimcereConfig.widgets.BBS);
    };
}).service('getBBSList', function($timeout) {
    return function(scope) {
        var bbsUrl = SimcereConfig.server.bbs + "/newest.php?jsoncallback=?&mod=thread&page=";
        appcan.window.openToast('正在加载');
        appcan.request.ajax({
            type : 'GET',
            url : bbsUrl,
            data : '',
            contentType : 'application/json',
            dataType : 'text',
            success : function(data, status, xhr) {
                appcan.window.closeToast();
                if (data.newThread instanceof Array) {
                    if (data.newThread.length > 3) {
                        data.newThread.length = 3;
                    }
                    data.newThread = data.newThread.map(function(o) {
                        if (o.avatar) {
                            o.avatarbg = 'background-image:url(' + o.avatar + ');';
                            return o;
                        }
                    });
                    $timeout(function() {
                        scope.posts = data.newThread;
                    }, 0);
                }
            },
            error : function(xhr, errorType, error) {
            }
        });
    };
}).service('openNNDetail', function() {
    return function(msgId, msgCode) {
        //localStorage.setItem('NN/detail.msgParam', msgId + ':' + msgCode);
        //localStorage.setItem('Index/index.openNNDetailFromMainApp', '1');
        uexAppCenterMgrEx.isAppInstalled(SimcereConfig.widgets.NN);
    };
}).service('initUpdateOnEvent', function(getNNList) {
    //订阅，并按事件刷新列表
    return function() {
        appcan.window.subscribe('NN/index.refresh', function() {
            getNNList.call();
        });
    };
}).service('isToday', function($filter) {
    return function(cmp, origin) {
        var toCmp;
        //神经网络和bbs格式不同，需要分别处理
        if (origin == 'bbs') {
            toCmp = $filter('formatTimeFromBBS')(cmp).substr(0, 8);
        } else {
            var tmp = $filter('toUnixTimestamp')(cmp);
            toCmp = $filter('date')(tmp, 'yy-MM-dd').substr(0, 8);
        }
        var today = $filter('date')(Date.now(), 'yy-MM-dd');
        return toCmp == today;
    };
}).controller('NNListController', function($scope, getNNList, openNNDetail, initUpdateOnEvent, isToday) {
    //域数据声明
    getNNList.call();
    $scope.openNNDetail = openNNDetail;

    initUpdateOnEvent.call();

    //todo: hack 刷新列表
    appcan.window.subscribe('Auth/index.loginSuccess', function() {
        getNNList.call();
    });
    //isToday
    $scope.isToday = isToday;
}).controller('bbsListController', function($scope, getBBSList, openBBS, isToday) {
    $scope.posts = [];
    $scope.openBBS = openBBS;
    getBBSList($scope);
    appcan.window.subscribe('BBS/main.syncCard', function() {
        getBBSList($scope);
    });
    //isToday
    $scope.isToday = isToday;
});

/**
 * 初始化门户插件
 */
function initChildAppHandler() {
    //绑定回调
    uexAppCenterMgrEx.cbClickMore = uexAppCenterMgrEx_cbClickMoreHandler;
    uexAppCenterMgrEx.cbSoftToken = uexAppCenterMgrEx_cbSoftToken;
    uexAppCenterMgrEx.cbClickMoreHandler = uexAppCenterMgrEx_cbClickMoreHandler;
    uexAppCenterMgrEx.cbInstallApp = uexAppCenterMgrEx_cbInstallApp;
    uexAppCenterMgrEx.cbDeleteApp = uexAppCenterMgrEx_cbDeleteApp;
    uexAppCenterMgrEx.cbStartNative = uexAppCenterMgrEx_cbStartNative;
    uexAppCenterMgrEx.cbAppInstalled = uexAppCenterMgrEx_cbAppInstalled;
    uexAppCenterMgrEx.cbHeightChanged = uexAppCenterMgrEx_cbHeightChanged;
    uexAppCenterMgrEx.startWidget = startWidget;

    var plgContainerEl = document.querySelector('.n_container_uexAppCenterMgrEx'),
        plgContainerStyle = window.getComputedStyle(plgContainerEl, null),
        posX = 0,
        posY = parseFloat(localStorage.getItem("simcere.runtime.uexAppCenterMgrEx.offset.y")),
        width = parseFloat(plgContainerStyle.width),
        height = parseFloat(plgContainerStyle.height),
        storeUrl = SimcereConfig.server.emm + "/storeIn/",
        w = window.screen.width, //手机屏幕宽度
        cw = parseFloat(localStorage.getItem("simcere.runtime.uexAppCenterMgrEx.offset.y") * 2),
        f = w / cw;
    //手机屏幕宽度与计算出来的层宽度比例

    //安卓系统部分机型需要像素换算
    //if ($.os.ios) {
    //    //iOS不需要特殊处理
    //} else {
    //    posY = parseFloat(posY) / (window.devicePixelRatio / 1.5);
    //}

    /**
     * @method 生成view并显示在界面上
     *
     * @param {string} inX x坐标，int类型
     * @param {string} inY x坐标，int类型
     * @param {string} inWidth 宽，int类型
     * @param {string} inHeight 高，int类型
     * @param {string} appurl 获取所有应用的url  例：http://192.168.1.1:8080/storeIn/
     * @param {string} adurl 当前版本没用,
     * todo: adurl临时传递主应用的id，将来需要自动获取
     */
    uexAppCenterMgrEx.open(parseFloat(posX), parseFloat(posY), parseFloat(width * f), parseFloat(height * f), storeUrl, JSON.stringify({
        "appId" : SimcereConfig.appId,
        "key" : SimcereConfig.appKey
    }));

}

/**
 * 门户回调Handler：
 * 获取softToken
 * @param token
 */
function uexAppCenterMgrEx_cbSoftToken(token) {
    //        alert('uexAppCenterMgrEx_cbSoftToken: '+token);
}

/**
 * 门户回调Handler：
 * 点击首页更多按钮时的回调
 */
function uexAppCenterMgrEx_cbClickMoreHandler() {
    //todo: android hack
    localStorage.setItem('simcere.runtime.doNotCallGesture', '1');
    setTimeout(function() {
        localStorage.removeItem('simcere.runtime.doNotCallGesture');
    }, 200);

    /*安卓系统由于兼容原因需要hack手势弹出问题屏蔽
     if(localStorage.getItem('simcere.runtime.hasGesturePassword')){
     localStorage.removeItem('simcere.runtime.hasGesturePassword');
     setTimeout(function () {
     localStorage.setItem('simcere.runtime.hasGesturePassword', '1');
     }, 500);
     }*/
}

/**
 * 门户回调Handler：
 * 安装应用时的回调
 * @param appId 应用的ID
 * @param appName 应用名称
 * @param version 应用版本号
 */
function uexAppCenterMgrEx_cbInstallApp(appId, appName, version) {
    //alert('uexAppCenterMgrEx_cbInstallApp: ' + appId + ':' + appName + ':' + version);
}

/**
 * 门户回调Handler：
 * 删除应用时的回调
 * @param appId 应用的ID
 * @param appName 应用名称
 * @param version 应用版本号
 */
function uexAppCenterMgrEx_cbDeleteApp(appId, appName, version) {
    //alert('uexAppCenterMgrEx_cbDeleteApp: ' + appId + ':' + appName + ':' + version);
}

/**
 * 门户回调Handler：
 * 启动原生应用的回调
 * @param o
 * o.platform
 * o.pkgName
 * o.key
 */
function uexAppCenterMgrEx_cbStartNative(o) {
    //alert('uexAppCenterMgrEx_cbStartNative: ' + o);
}

/**
 * 门户回调Handler：
 * 判断子应用是否安装
 * @params appId
 * @params status
 */
function uexAppCenterMgrEx_cbAppInstalled(appId, status) {
    //alert('uexAppCenterMgrEx_cbAppInstalled: ' + appId + ':' + status + ':' + typeof status);
    console.log('uexAppCenterMgrEx_cbAppInstalled:' + Date.now());
    if (status == 0) {
        uexWindow.alert('提示', '请点击"添加"按钮安装子应用', '确定');
    } else {
        // refresh bbs cookie life cycle
        if (appId == SimcereConfig.widgets.BBS) {
            zkutil.bbsLoginHandler();
        }
        uexWidget.startWidget(appId, '10', '', '');
    }
}

/**
 * 门户回调Handler：
 * 磁贴高度发生变化时的回调
 * @param {string} height
 */
function uexAppCenterMgrEx_cbHeightChanged(height) {
    var h = parseFloat(height, 10);
    var w = window.screen.width;
    //手机屏幕宽度
    var cw = parseFloat(localStorage.getItem("simcere.runtime.uexAppCenterMgrEx.offset.y") * 2);
    var f = w / cw;
    //手机屏幕宽度与计算出来的层宽度比例
    $('.n_container_uexAppCenterMgrEx').css('height', h / f);
}

/**
 * 门户回调Handler：
 * 打开子应用的回调
 * @param widgetId 子应用的id
 * @param text 暂无用
 * @param widgetKey 子应用的key
 */
function startWidget(widgetId, text, widgetKey) {
    //todo: android hack
    localStorage.setItem('simcere.runtime.doNotCallGesture', '1');
    setTimeout(function() {
        localStorage.removeItem('simcere.runtime.doNotCallGesture');
    }, 200);

    // refresh bbs cookie life cycle
    if (widgetId == SimcereConfig.widgets.BBS) {
        zkutil.bbsLoginHandler();
    }
    /**
     * 加载一个widget
     * @param {string} appId 子widget的appId
     * @param {string} animiId 子widget载入时的动画id
     * @param {string} funName 方法名，子widget结束时将String型的任意字符回调给该方法，可为空。 注意：只在主窗口中有效，浮动窗口中无效
     * @param {string} info 传给子widget的信息
     * @param {string} [animDuration] 动画持续时长，单位为毫秒，默认200毫秒
     */
    console.log('uexWidget.startWidget');
    uexWidget.startWidget(widgetId, '10', '', '');
}

/**
 * 显示消息数
 * @param appId 子应用id
 * @param msgCount 消息数
 */
var setMsgCount = (function() {
    var msgList = [];
    return function(appId, msgCount) {
        msgCount = msgCount > 99 ? 99 : msgCount;
        msgList = _.reject(msgList, function(msg) {
            return msg.appId == appId;
        });
        msgList.push({
            appId : appId,
            num : msgCount
        });
        msgList.sort(function(a, b) {
            return a.appId > b.appId;
        });
        var param = JSON.stringify(msgList);
        console.log("uexAppCenterMgrEx.showNewMsg: " + param);
        uexAppCenterMgrEx.showNewMsg(param);
    };
})();

/**
 * 子应用同步助手
 * 用于主应用升级、整包更新时，若检查到当前实际版本号与localStorage中的版本号不一致，
 * 则应该将设置精品的子应用删除，防止出现版本号与实际包不一致的问题
 */
function syncWgtsHelper(){
    uexWidgetOne.cbGetCurrentWidgetInfo = function (opId, dataType, data) {
        var versionKey = 'simcere.runtime.appVersion';
        var version = localStorage.getItem(versionKey);

        var wgtObj = JSON.parse(data);
        if (wgtObj.version == version) {
            console.log('wgtObj.version == version');
        }else{
            //执行子应用同步助手，仅当全部成功处理时更新versionKey
            preInstallAppsHandler(function () {
                localStorage.setItem(versionKey, wgtObj.version);
            });
        }

    };
    uexWidgetOne.getCurrentWidgetInfo();
}

//测试
function setMsgCount2() {
    var widgets = ['NN', 'BBS', 'Contacts'];
    var wgtName = widgets[_.random(0, 2)];
    var appId = SimcereConfig.widgets[wgtName];
    var num = _.random(0, 200);
    setMsgCount(appId, num);
}

setTimeout(function() {
    document.querySelector('.j-btn-badge').addEventListener('click', setMsgCount2);
}, 200);
