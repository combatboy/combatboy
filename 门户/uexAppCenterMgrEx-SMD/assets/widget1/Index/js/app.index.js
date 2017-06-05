//todo: remove fallback
if(!localStorage.getItem('simcere.runtime.userId')){
    //localStorage.setItem('simcere.runtime.userId', '13205');
    //localStorage.setItem('simcere.runtime.userId', '12222');
}

appcan.ready(function () {
    initSlider();

    //清除(关闭)登录窗口
    zkutil.closeWindowByName('Auth_index');

    var titHeight = $(document.body).offset().width * 0.5;
    localStorage.setItem("simcere.runtime.uexAppCenterMgrEx.offset.y", titHeight);
    appcan.frame.open("content", "index_content.html", 0, titHeight);
    window.onorientationchange = window.onresize = function () {
        appcan.frame.resize("content", 0, titHeight);
    };

    // 监听返回按钮
    zkutil.setReportKey(0, 1, function (keyCode) {
        if(keyCode==0){
            uexWidgetOne.exit();
        }
    });
});

//
function initSlider() {
    appcan.request.ajax({
        type: 'GET',
        url: SimcereConfig.server.mas + '/news/getLatestNewsList',
        data: {},
        contentType: 'application/json',
        dataType: 'json',
        success: function (data, status, xhr) {
            console.log(data);
            if(data.status!='0'){
                console.error('res error');
            }else if(data.data instanceof Array && data.data.length){
                initSlider.setSlides(data.data);
            }else{
                console.error('res is empty');
            }
        },
        error: function (xhr, errorType, error) {
            console.error('network error');
            //appcan.window.openToast('网络连接不可用', 2000);

            failHandler.call();
        }
    });
}
initSlider.failCount = 0;
initSlider.failHandler = function (){
    initSlider.failCount++;
    if (initSlider.failCount < 3) {
        initSlider();
    }
};
initSlider.setSlides = function (arr) {
    var tpl = '<div' +
            ' class="swiper-slide ub-img1"' +
            ' data-msgId="<%-msgId%>"' +
            ' data-type="<%-type%>"' +
            ' data-label="<%-label%>"' +
            ' data-author="<%-author%>"' +
            ' data-createdAt="<%-createdAt%>"' +
            ' data-data="<%-data%>"' +
            ' style="background-image: url(<%-img%>)">' +
            '</div>';
    var tplCompiler = _.template(tpl);
    var html = _.map(arr, function (o) {
        var obj = {
            msgId: o.id,
            type: o.type,
            label: o.type=='专题'?'专题：'+o.title:'新闻：'+o.title,
            img: o.icon,
            createdAt: o.createdAt,
            author: o.author
        };
        obj.data = JSON.stringify(obj);
        return tplCompiler(obj);
    });
    var sliederLen = html.length;
    $('.swiper-wrapper').html(html.join(''));

    var mySwiper = new Swiper ('.m-slider', {
        pagination: '.swiper-pagination',
        paginationClickable: sliederLen > 1,
        loop: true,
        threshold: sliederLen > 1 ? 0 : 9999,
        autoplay: sliederLen > 1 ? 6000 : undefined,
        autoplayDisableOnInteraction: false,
        onSlideChangeEnd: function (swiper) {
            var currSlide = swiper.slides[swiper.activeIndex];
            var txt = $(currSlide).data('label');
            //设置label
            $('.n-swiper-label').text(txt);
        },
        onTap: function(swiper, event){
            //点击pager也会触发事件，需要判断
            var target = event.target;
            if(target.className.indexOf('swiper-slide')==-1){
                return;
            }
            //数据
            var s = target.dataset['data'];
            localStorage.setItem('News/index.slide', s);
            appcan.window.open('News_newsDetail','../../News/html/newsDetail.html',10);
        }
    });
};