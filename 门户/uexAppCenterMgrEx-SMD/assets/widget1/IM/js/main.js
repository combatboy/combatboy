var isPhone = (window.navigator.platform != "Win32");
var isAndroid = (window.navigator.userAgent.indexOf('Android')>=0)?true : false;

function $$(id){
    return document.getElementById(id);
}

//本地存储
var lcstor = window.localStorage;
function setStorage(key,value){
    if(lcstor)
        lcstor[key] = value;
    else
        alert('localStorage error');
}
function getStorage(key){
    if(lcstor){
        for(i in lcstor){
            if(i==key)
                return lcstor[i];
        }
    }else
        alert('localStorage error');
}
function clearStorage(key){
    if(lcstor){
        if(key)
            lcstor.removeItem(key);
        else
            lcstor.clear();
    }
    else
        alert('localStorage error');
}
//如果字段为空，接口返回{}，此时改为空字符串
function  judgeNull(id){
    if (id == "[object Object]") {
        id = "";
    }
    return id;
}

//清除左右空格     
function trim(str){ //删除左右两端的空格
　　   return str?str.replace(/(^\s*)|(\s*$)/g, ""):"";
}
function $toast(mes,t){
    uexWindow.toast(t?'0':'1','5',mes,t?t:0);
}
function logs(m){
    uexLog.sendLog(m);
}

//日期补零
function addZero(n){
    if(n<10){
        return '0' + n;     
    }
    return '' + n;
}

/**
 * 设置平台弹动效果
 * @param Function downcb 下拉
 * @param Function upcb   上拖
 */
function setPageBounce1(downcb, upcb){
    if(!isPhone)    return;
    uexWindow.setBounce("1");
    var top = 0;var btm = 1;
   
    if(!downcb && !upcb){
        uexWindow.showBounceView(top,"#f1f1f1","0");
        uexWindow.showBounceView(btm,"#f1f1f1","0");
        return;
    }
    uexWindow.onBounceStateChange = function(type,state){   
        if(type==top && state==2)       downcb();
        if(type==btm && state==2)       upcb();
    }
    if(downcb){
        uexWindow.setBounceParams('0','{"imagePath":"res://jiantou.png","pullToReloadText":"下拉刷新","releaseToReloadText":"释放刷新","loadingText":"加载中,请稍候"}','donghang');
        uexWindow.showBounceView(top,"#f1f1f1",1);
        uexWindow.notifyBounceEvent(top,1); 
    }
    if(upcb){
        uexWindow.setBounceParams('1','{"imagePath":"res://jiantou.png","pullToReloadText":"加载更多","releaseToReloadText":"加载更多","loadingText":"加载中,请稍候"}');
        uexWindow.showBounceView(btm,"#f1f1f1",1);//设置弹动位置及效果([1:显示内容;0:不显示])
        uexWindow.notifyBounceEvent(btm,1);       //注册接收弹动事件([0:不接收onBounceStateChange方法回调;1:接收])
    }
}

/***
 * 使弹动重置为初始位置
 * @param String type 弹动的类型 0-顶部弹动  1-底部弹动 
 */
function resetBV(type){
    uexWindow.resetBounceView(type);

}

/**
 * 手动关闭加载框
 */
function $closeToast(){
    uexWindow.closeToast();
}
function alert(msg){
    appcan.window.alert('',msg);
}

function clearLoc(str){
    var storage = appcan.locStorage.keys();
    for(var i in storage){
        var key = storage[i];
        if(key.indexOf(str)>=0){
           appcan.locStorage.remove(key);
        }
     }
}

function cutReason(str){
    if(str.length>20){
        return str.substr(0,20)+'...';
    }
    return str;
}
