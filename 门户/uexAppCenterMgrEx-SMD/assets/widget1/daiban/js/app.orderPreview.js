/**
 * Created by mitty on 16/3/15.
 */
angular.module('myApp', ['ngq'])
        .service('Data', function () {
            return {
                order: null,//订单详情
                orderDetail: [],//订单临时明细
                orderDetailNC: [],//订单NC明细
                accept: [],//承兑汇票明细
                onlinePayment: [],//网上付款明细
                saleout: [],//销售出库单明细

                itemList: [],
                itemListErr: false,
                itemListEmpty: false,

                priceTypeName: undefined,//价格项名称

                DebtMoney: [],
                DebtMoneyErr: false,
                DebtMoneyEmpty: false,
                dropDebtOpen: false,

                DebtDays: [],
                DebtDaysEmpty: false,
                DebtDaysErr: false,
                dropDebtDaysOpen: false,
                maxDebtDays: undefined,

                DebtSum: undefined,//欠款总额
                orderSum: undefined,//本单总额

                hasDQPZ: false,//当期票折
                memo: '备注',
                NoPayment: 0,

                customer: null//客户，从中可取得【业务线】信息
            };
        })
        .service('getCustomer', function ($timeout, Data) {
            return function (cusCode,comCode){
                var params = {
                    "loginId": localStorage.getItem('simcere.runtime.loginId'),
                    "comCode": comCode,
                    "cusCode": cusCode,
                    "pageNumber": "1",
                    "pageSize": "2",
                    "searchContent": ""
                };
                console.log(params);
                //appcan.window.openToast('请稍候');
                appcan.request.ajax({
                    type: 'POST',
                    url: SimcereConfig.server.mas + '/ship/ScrQueryCustomerByUserAndCom',
                    data: params,
                    contentType: 'application/json',
                    dataType: 'json',
                    success: function (data, status, xhr) {
                        //appcan.window.closeToast();
                        //appcan.frame.resetBounce(1);
                        //appcan.window.resetBounceView(1);//0:顶端，1:底端
                        console.log(data);

                        if(data.status!='0'){
                            //appcan.window.openToast('操作失败',2000);
                            console.error('res error');
                        }else{
                            $timeout(function () {
                                Data.customer = data.data[0];
                            });
                        }
                    },
                    error: function (xhr, errorType, error) {
                        //appcan.window.openToast('网络连接不可用',2000);
                        //appcan.frame.resetBounce(1);
                        //appcan.window.resetBounceView(1);//0:顶端，1:底端
                        //appcan.window.openToast('网络连接不可用', 2000);
                        console.error('network error');
                    }
                });
            };
        })
        .service('getOrderDetail', function ($timeout, Data, getDebtMoney, getDebtDays, getNoPayment, getCustomer) {
            return function (orderId) {
                var params = {
                    id: orderId,//3
                    loginId: localStorage.getItem('simcere.runtime.loginId')
                };

                console.log(params);
                appcan.request.ajax({
                    type: 'POST',
                    url: SimcereConfig.server.mas + '/ship/ScrQueryOrderById',
                    data: params,
                    contentType: 'application/json',
                    dataType: 'json',
                    success: function (data, status, xhr) {
                        console.log(data);

                        if (data.status != '0') {
                            $timeout(function () {
                                Data.itemListErr = true;
                            });
                            console.error('res error');
                        } else {
                            var od = data.order || data.oreder;

                            Data.accept.forEach(function (o, i, a) {
                                o.urls= o.url.split(',');
                            });
                            Data.onlinePayment.forEach(function (o, i, a) {
                                o.urls= o.url.split(',');
                            });

                            $timeout(function () {
                                Data.order = od[0];
                                Data.orderDetail = data.orderDetail;
                                Data.orderDetailNC = data.orderDetailNC;
                                Data.accept = data.accept;
                                Data.onlinePayment = data.onlinePayment;
                                Data.saleout = data.saleout;

                                //判断结果是否为空
                                Data.itemListEmpty = !Data.orderDetail.length;

                                //欠款金额
                                getDebtMoney(od[0].CUSCODE, od[0].COMCODE);
                                //欠款天数
                                getDebtDays(od[0].CUSCODE, od[0].COMCODE);
                                //未下账单金额总额
                                getNoPayment(od[0].CUSCODE, od[0].COMCODE);

                                //getCustomer
                                getCustomer(od[0].CUSCODE,od[0].COMCODE);
                            });
                        }
                    },
                    error: function (xhr, errorType, error) {
                        //appcan.window.openToast('网络连接不可用', 2000);
                        console.error('network error');
                        $timeout(function () {
                            Data.itemListErr = true;
                        });
                    }
                });
            };
        })
        .controller('GlbController', function ($scope, Data, getOrderDetail, modifyOrder, deleteOrder, submitOrder,
                                               chaoziHandler, piaozheHandler, ngqViewImages) {
            $scope.Data = Data;
            $scope.ngqViewImages = ngqViewImages;

            //根据请求页确定订单id
            var orderId;
            var dataSource = $('#data-source').data('source');
            console.log('数据源：'+dataSource);

            if(dataSource=='chaozi'){
                orderId = localStorage.getItem('daiban/chaoziDetail.orderId');
            }else if(dataSource=='piaozhe'){
                orderId = localStorage.getItem('daiban/piaozheDetail.orderId')||222;
            }else{
                throw new ReferenceError('数据源丢失');
            }
            if(!orderId){
                throw new ReferenceError('订单ID丢失');
            }
            getOrderDetail(orderId);

            //编辑订单
            appcan.window.subscribe('SO/orderPreview.modify', function () {
                var orderId = Data.order.ID;
                modifyOrder(orderId);
            });
            //删除订单
            appcan.window.subscribe('SO/orderPreview.delete', function () {
                appcan.window.confirm({
                    title:'提示',
                    content:'确认要删除订单吗？',
                    buttons:['确定','取消'],
                    callback:function(err,data,dataType,optId){
                        if(err){
                            console.error('appcan.window.confirm: button error.');
                        }else if(data==0){
                            var orderId = Data.order.ID;
                            deleteOrder(orderId);
                        }
                    }
                });

            });
            //提交订单
            appcan.window.subscribe('SO/orderPreview.submit', function () {
                var orderId = Data.order.ID;
                submitOrder(orderId);
            });


            //审核:超资信
            appcan.window.subscribe('daiban/chaoziDetail.passBack', function (passBack) {
                console.log('daiban/chaoziDetail.passBack');
                var orderId = Data.order.ID;
                window.zyModalCallback = function (comment) {
                    chaoziHandler(orderId, passBack, comment);
                };
                openZyModal();
            });

            //审核:票折
            appcan.window.subscribe('daiban/piaozheDetail.passBack', function (passBack) {
                console.log('daiban/piaozheDetail.passBack');
                var orderId = Data.order.ID;
                window.zyModalCallback = function (comment) {
                    piaozheHandler(orderId, passBack, comment);
                };
                openZyModal();
            });

            //模态窗回调
            appcan.window.subscribe('zyModal.close', function (comment) {
                console.log('zyModal.close');
                if(typeof window.zyModalCallback == 'function'){
                    window.zyModalCallback(comment);
                }
            });

        })
        .service('getNoPayment', function (Data, $timeout) {
            return function (cusCode, comCode) {
                var params = {
                    "loginId": localStorage.getItem('simcere.runtime.loginId'),
                    "cusCode": cusCode,
                    "comCode": comCode,
                    "id": ""//TODO:亟待确认
                };
                //todo:如果是修改订单，参数中需要传递id

                appcan.request.ajax({
                    type: 'POST',
                    url: SimcereConfig.server.mas + '/ship/ScrNoPayment',
                    data: params,
                    contentType: 'application/json',
                    dataType: 'json',
                    success: function (data, status, xhr) {
                        //console.log(data);
                        if (data.status != '0') {
                            console.error('res error');
                        } else {
                            $timeout(function () {
                                Data.NoPayment = data.data;
                                console.log('not payment: ' + data.data);
                            });
                        }
                    },
                    error: function (xhr, errorType, error) {
                        console.error('network error');
                        //appcan.window.openToast('网络连接不可用', 2000);
                    }
                });
            };
        })
        .service('getDebtMoney', function ($timeout, Data, calcDebtSum) {
            return function (cusCode, comCode) {

                var params = {
                    "loginId": localStorage.getItem('simcere.runtime.loginId'),
                    "cusCode": cusCode,
                    "comCode": comCode
                };

                appcan.request.ajax({
                    type: 'POST',
                    url: SimcereConfig.server.mas + '/ship/ScrQueryOweMoney',
                    data: params,
                    contentType: 'application/json',
                    dataType: 'json',
                    success: function (data, status, xhr) {
                        console.log(data);

                        if (data.status != '0') {
                            $timeout(function () {
                                Data.DebtMoneyErr = true;
                            });
                        } else {
                            var objArray = _.groupBy(data.data, function (obj) {
                                return obj.VRECEIPTCODE;
                            });
                            var arr = _.map(objArray, function (o) {
                                return o;
                            });
                            $timeout(function () {
                                Data.DebtMoney = arr;
                                if (arr.length == 0) {
                                    Data.DebtMoneyEmpty = true;
                                }
                                //计算欠款总额
                                calcDebtSum.call(null, data.data);
                            });

                        }
                    },
                    error: function (xhr, errorType, error) {
                        console.error('network error');
                        //appcan.window.openToast('网络连接不可用', 2000);
                        $timeout(function () {
                            Data.DebtMoneyErr = true;
                        });
                    }
                });
            };
        })
        .service('getDebtDays', function ($timeout, Data) {
            return function (cusCode, comCode) {

                var params = {
                    "loginId": localStorage.getItem('simcere.runtime.loginId'),
                    "cusCode": cusCode,
                    "comCode": comCode
                };

                appcan.request.ajax({
                    type: 'POST',
                    url: SimcereConfig.server.mas + '/ship/ScrQueryOweDay',
                    data: params,
                    contentType: 'application/json',
                    dataType: 'json',
                    success: function (data, status, xhr) {

                        if (data.status != '0') {
                            $timeout(function () {
                                Data.DebtDaysErr = true;
                            });
                        } else {
                            //最大欠款天数
                            var maxDebtDays;
                            if(!data.data.length){
                                maxDebtDays = '0';
                            }else{
                                maxDebtDays = _.max(data.data, function (o) {
                                    console.log(o.DAYS * 1);
                                    return o.DAYS * 1;
                                }).DAYS;
                            }

                            var objArray = _.groupBy(data.data, function (obj) {
                                return obj.JSFPH;
                            });
                            var arr = _.map(objArray, function (o) {
                                return o;
                            });

                            $timeout(function () {
                                Data.DebtDays = arr;
                                Data.maxDebtDays = maxDebtDays;

                                if (arr.length == 0) {
                                    Data.DebtDaysEmpty = true;
                                }
                            });
                        }
                    },
                    error: function (xhr, errorType, error) {
                        console.error('network error');
                        //appcan.window.openToast('网络连接不可用', 2000);
                        $timeout(function () {
                            Data.DebtDaysNetErr = true;
                        });
                    }
                });
            };
        })
        .service('calcDebtSum', function (Data) {
            //欠款求和
            return function (list) {
                Data.DebtSum = _.reduce(list, function (memo, item) {
                    return memo + item.MONEY;
                }, 0);
            };
        })
        .service('calcOrderSum', function ($timeout, Data) {
            //本单求和
            return function () {
                Data.orderSum = _.reduce(Data.itemList, function (memo, item) {
                    return memo + item.PRICE * item.orderNum;
                }, 0);
            };
        })
        .service('modifyOrder', function () {
            return function (orderId) {
                appcan.window.alert('提示','功能开发中');
                return;
                //todo:
                console.log('todo: 修改订单');
                localStorage.setItem('SO/orderPreview.orderIdModify', orderId);

                qlib.closeWindowByName([
                    'SO_create3',
                    'SO_create4',
                    'SO_create5'
                ]);

                //todo:
                modifyOrderPrepareStuff(orderId);
                appcan.window.open('SO_create2', 'create2.html', 0);
            };
        })
        .service('deleteOrder', function () {
            return function (orderId) {
                var params = {
                    id: orderId
                };
                console.log('删除订单');
                console.log(params);
                appcan.window.openToast('请稍候');
                appcan.request.ajax({
                    type: 'POST',
                    url: SimcereConfig.server.mas + '/ship/ScrDeleteOrderById',
                    data: params,
                    contentType: 'application/json',
                    dataType: 'json',
                    success: function (data, status, xhr) {
                        appcan.window.closeToast();
                        //appcan.frame.resetBounce(1);
                        //appcan.window.resetBounceView(1);//0:顶端，1:底端
                        console.log(data);

                        if(data.status!='0'){
                            appcan.window.openToast('操作失败',2000);
                            console.error('res error');
                        }else if(data.data.status!='0'){
                            appcan.window.openToast('操作失败',2000);
                            console.error('res error2');
                        }else{
                            appcan.window.openToast(data.data.msg,2000);
                            qlib.closeWindowByName([
                                'SO_create1',
                                'SO_create2',
                                'SO_create3',
                                'SO_create4',
                                'SO_create5'
                            ]);
                            setTimeout(function () {
                                qlib.closeWindowByName('SO_orderPreview');
                            }, 2000);
                        }
                    },
                    error: function (xhr, errorType, error) {
                        appcan.window.openToast('网络连接不可用',2000);
                        //appcan.frame.resetBounce(1);
                        //appcan.window.resetBounceView(1);//0:顶端，1:底端
                        console.error('network error');
                    }
                });
            };
        })
        .service('submitOrder', function () {
            return function (orderId) {
                var params = {
                    id: orderId
                };
                console.log('提交订单：'+orderId);

                appcan.window.openToast('请稍候');
                appcan.request.ajax({
                    type: 'POST',//todo
                    url: SimcereConfig.server.mas + '/ship/ScrCommitOrder',
                    data: params,
                    contentType: 'application/json',
                    dataType: 'json',
                    success: function (data, status, xhr) {
                        appcan.window.closeToast();
                        //appcan.frame.resetBounce(1);
                        //appcan.window.resetBounceView(1);//0:顶端，1:底端
                        console.log(data);
                        
                        if(data.status!='0'){
                            appcan.window.openToast('操作失败',2000);
                            console.error('res error');
                        }else if(data.data.status!='0'){
                            appcan.window.openToast('操作失败',2000);
                            console.error('res error2');
                        }else{
                            appcan.window.openToast(data.data.msg,2000);
                            qlib.closeWindowByName([
                                'SO_create1',
                                'SO_create2',
                                'SO_create3',
                                'SO_create4',
                                'SO_create5'
                            ]);
                            setTimeout(function () {
                                qlib.closeWindowByName('SO_orderPreview');
                            }, 2000);
                        }
                    },
                    error: function (xhr, errorType, error) {
                        appcan.window.openToast('网络连接不可用',2000);
                        //appcan.frame.resetBounce(1);
                        //appcan.window.resetBounceView(1);//0:顶端，1:底端
                        //appcan.window.openToast('网络连接不可用', 2000);
                        console.error('network error');
                    }
                });
            };
        })
        .service('chaoziHandler', function ($filter) {
            return function (orderId, backPass, comment) {
                /**
                 * id指定的是订单id,
                 * status值为3表示超资信审核通过，status值为4表示超资信审核不通过，
                 * czxcomment表示审批意见
                 */
                var params = {
                    "id": orderId,
                    "status": backPass == 'pass' ? 3 : 4,
                    "czxshr": localStorage.getItem('simcere.runtime.loginId'),
                    "czxshsj": $filter('date')(Date.now(), 'yyyy-MM-dd HH:mm:ss'),
                    "czxcomment": comment||'未填写'
                };
                console.log('chaoziHandler:');
                console.log(params);

                appcan.window.openToast('请稍候');
                appcan.request.ajax({
                    type: 'POST',
                    url: SimcereConfig.server.mas + '/ship/ScrApproveCredit',
                    data: params,
                    contentType: 'application/json',
                    dataType: 'json',
                    success: function (data, status, xhr) {
                        appcan.window.closeToast();
                        //appcan.frame.resetBounce(1);
                        //appcan.window.resetBounceView(1);//0:顶端，1:底端
                        console.log(data);

                        if(data.status!='0'){
                            appcan.window.openToast('操作失败',2000);
                            console.error('res error');
                        } else if(data.data.status!='0'){
                            appcan.window.openToast(data.data.msg,2000);
                        } else {
                            appcan.window.openToast(data.data.msg,2000);
                            //通知刷新、关闭当前窗口
                            setTimeout(function () {
                                appcan.window.publish('daiban/list2.change','');
                                qlib.closeWindowByName(['daiban_chaoziDetail','daiban_piaozheDetail']);
                            }, 2000);
                        }
                    },
                    error: function (xhr, errorType, error) {
                        appcan.window.openToast('网络连接不可用',2000);
                        //appcan.frame.resetBounce(1);
                        //appcan.window.resetBounceView(1);//0:顶端，1:底端
                        console.error('network error');
                    }
                });
            };
        })
        .service('piaozheHandler', function ($filter) {
            return function (orderId, backPass, comment){
                /**
                 * id指定的是订单id,
                 * status值为3表示票折审核通过，status值为4表示票折审核不通过，
                 * pzcomment表示审批意见
                 */
                var params = {
                    "id": orderId,
                    "status": backPass == 'pass' ? 3 : 4,
                    "pzshr": localStorage.getItem('simcere.runtime.loginId'),
                    "pzshsj": $filter('date')(Date.now(), 'yyyy-MM-dd HH:mm:ss'),
                    "pzcomment": comment||'未填写'
                };
                console.log('piaozheHandler:');
                console.log(params);

                appcan.window.openToast('请稍候');
                appcan.request.ajax({
                    type: 'POST',
                    url: SimcereConfig.server.mas + '/ship/ScrApproveDiscount',
                    data: params,
                    contentType: 'application/json',
                    dataType: 'json',
                    success: function (data, status, xhr) {
                        appcan.window.closeToast();
                        //appcan.frame.resetBounce(1);
                        //appcan.window.resetBounceView(1);//0:顶端，1:底端
                        console.log(data);

                        if(data.status!='0'){
                            appcan.window.openToast('操作失败',2000);
                            console.error('res error');
                        } else if(data.data.status!='0'){
                            appcan.window.openToast(data.data.msg,2000);
                        } else {
                            appcan.window.openToast(data.data.msg,2000);
                            //通知刷新、关闭当前窗口
                            setTimeout(function () {
                                appcan.window.publish('daiban/list2.change','');
                                qlib.closeWindowByName(['daiban_chaoziDetail','daiban_piaozheDetail']);
                            }, 2000);
                        }
                    },
                    error: function (xhr, errorType, error) {
                        appcan.window.openToast('网络连接不可用',2000);
                        //appcan.frame.resetBounce(1);
                        //appcan.window.resetBounceView(1);//0:顶端，1:底端
                        console.error('network error');
                    }
                });
            };
        });
