/**
 * 缓存图片
 * 在线时只缓存图片并不执行设置背景的操作（使用第四个参数）
 * 离线时使用缓存设置背景的操作
 *
 * @param data [] 图片数组
 * @param type 类型，0为在线，1为离线
 */
function cachePictures(data, type) {
    if (type == 0) {
        for (var i = 0; i < data.length; i++) {
            zy_imgcache("welocompage_" + i, data[i], data[i], function () {
            });
        }
    } else if (type == 1) {
        for (var i = 0; i < data.length; i++) {
            zy_imgcache("welocompage_" + i, data[i], data[i]);
        }
    }
}


/**
 * 在线获取多欢迎
 */
function onlineWelcomePage() {
    //appcan.window.openToast('请稍候');
    appcan.request.ajax({
        type: 'GET',
        url: SimcereConfig.server.mas + '/web/getLatestWelcomePackage',
        data: '',
        contentType: 'application/json',
        dataType: 'json',
        timeout: 2000,
        success: function (data, status, xhr) {
            //每组slider仅启动一次
        if(data.data && data.data.id){
            var thisSliderGroupId = data.data.id;
            var lastSliderGroupId = localStorage.getItem('root.lastSliderGroupId');
            if(thisSliderGroupId==lastSliderGroupId){
                console.log('运行过的欢迎页id:'+thisSliderGroupId);
                toLogin(0);
                return;
            }else{
                localStorage.setItem('root.lastSliderGroupId', thisSliderGroupId);
            }
        }

        if (data.status != 0) {
            toLogin(0);
        } else if (data.data
                && data.data.images instanceof Array
                && data.data.images.length) {
        var result = data.data;

        localStorage.setItem("root/Welcome", JSON.stringify(result));
        setWelcomePage(result);
        //缓存图片,在线
        cachePictures(result.images, 0);
        } else {
                toLogin(0);
            }
        },
        error: function (xhr, errorType, error) {
            console.error('network error: Failed to get welcomePage');
            toLogin(0);
        }
    });
}

/**
 * 离线获取多欢迎页
 */
function offlineWelcomePage() {
    if (localStorage.getItem("root/Welcome") && localStorage.getItem("root/Welcome") != "") {
        var data = JSON.parse(localStorage.getItem("root/Welcome"));
        setWelcomePage(data);
        //缓存图片，离线
        cachePictures(data.images, 1);
    } else {
        toLogin(0);
    }

}

/**
 * 跳转到登录页或者手势页
 *
 * @param type {number} 0:点击跳过或者立即体验 1:左划进入
 */
function toLogin(type) {
    try{
        appcanReadyContinue();
    }catch(ex){
        alert(ex.message);
    }
    /* 0：点击跳过或者立即体验   1：左划进入
    if (type == 0) {
        if (isHandLogin == '' || isHandLogin == '0') {//未启用手势密码，直接跳转至登录页面
            openwin('login', 'login1.html', 5);
        } else {
            initLockPattern();
            slider.moveToLast();
        }
    } else if (type == 1) {
        if (isHandLogin == '' || isHandLogin == '0') {//未启用手势密码，直接跳转至登录页面
            openwin('login', 'login1.html', 2);
        } else {
            initLockPattern();
            slider.moveToLast();
        }
    }*/

}

/**
 * 设置欢迎页
 *
 * @param result
 */
function setWelcomePage(result) {
    // 持续时间
    var duration = result.duration * 1000;
    // 是否允许跳过
    // 0表示允许1表示不允许
    var skip = result.skip;
    // 打开方式
    // 0表示显示立即体验按钮，1表示向左划，2表示立即体验按钮和向左划
    var enterType = result.enterType;
    // 图片数组
    var images = result.images;
    // 图片包 ID
    var packageId = result.id;

    slider = appcan.WelcomePageSlider({
        selector: '#yinSlide',
        hasIndicator: false,
        canDown: false,
        hasCircle: (images.length > 1),
        hasLabel: false,
        aspectRatio: 0,
        index: 0,
        swipeLeftInto: !!(enterType == 1 || enterType == 2),
        swipeLeftIntoCb: function () {
            setTimeout(function () {
                toLogin(1);
                slider.moveToLast();
            }, 200);
        }
    });

    var imageData = [];
    for (var i = 0; i < images.length; i++) {
        imageData.push({
            img: images[i],
            label: "",
            id: "welocompage_" + i
        });
    }

    slider.set(imageData);
    slider.on("swipeLeft", function (index, content) {
        if (index >= images.length) {
            document.getElementById("skipBtn").className = "ub-img uc-a1 uhide";

            if (enterType == 0) {
                document.getElementById("enterBtn").className = "ub-img uc-a1";

            } else if (enterType == 1) {

            } else if (enterType == 2) {
                document.getElementById("enterBtn").className = "ub-img uc-a1";
            }

        } else if (index < images.length) {
            document.getElementById("enterBtn").className = "ub-img uc-a1 uhide";

            if (skip == 0) {
                document.getElementById("skipBtn").className = "ub-img uc-a1";
            } else if (skip == 1) {
                document.getElementById("skipBtn").className = "ub-img uc-a1 uhide";
            }
        }

    });

    // 向右划
    slider.on("swipeRight", function (index, content) {
        if (index < images.length) {
            document.getElementById("enterBtn").className = "ub-img uc-a1 uhide";
            if (skip == 0) {
                document.getElementById("skipBtn").className = "ub-img uc-a1";
            } else if (skip == 1) {
                document.getElementById("skipBtn").className = "ub-img uc-a1 uhide";
            }
        }

    });

    // 图片长度为1时
    //      如果允许跳过并且进入方式为0、2就显示立即体验按钮
    //      如果不允许跳过就倒计时自动进入
    // 图片长度大于1时
    //      隐藏立即体验按钮
    //      如果允许跳过显示跳过按钮
    //      如果不允许跳过隐藏跳过按钮
    if (result.images.length == 1) {
        if (skip == 0) {
            if (enterType == 0) {
                document.getElementById("enterBtn").className = "ub-img uc-a1";
            } else if (enterType == 1) {

            } else if (enterType == 2) {
                document.getElementById("enterBtn").className = "ub-img uc-a1";
            }
        } else if (skip == 1) {
            setTimeout(function () {
                toLogin(0);
            }, duration);
        }
    } else if (result.images.length > 1) {
        document.getElementById("enterBtn").className = "ub-img uc-a1 uhide";
        if (skip == 0) {
            document.getElementById("skipBtn").className = "ub-img uc-a1";
        } else if (skip == 1) {
            document.getElementById("skipBtn").className = "ub-img uc-a1 uhide";
        }
    }
}

/**
 *检查网络
 *返回值：-1=网络不可用  0=WIFI网络  1=3G网络  2=2G网络
 */
function checkNetwork() {
    uexDevice.cbGetInfo = function (opCode, dataType, data) {
        var device = JSON.parse(data);
        var connectStatus = device.connectStatus;
        if (connectStatus != null && connectStatus.length > 0) {
            if (connectStatus == -1) {
                console.log('offline');
                offlineWelcomePage();
            } else {
                console.log('online');
                onlineWelcomePage();
            }
        }
        if (device.os != null && device.os.length > 0) {
            var ver = parseInt(device.os);
            if (ver > 6) {
                uexWindow.setStatusBarTitleColor('0');
                localStorage.setItem("IOS7fg", 1);
            } else {
                localStorage.setItem("IOS7fg", 0);
            }
        }
    };
    uexDevice.getInfo('13');
    uexDevice.getInfo('1');
}