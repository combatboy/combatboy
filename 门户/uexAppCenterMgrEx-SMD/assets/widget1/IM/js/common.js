var isPhone = (window.navigator.platform != "Win32");
var isAndroid = (window.navigator.userAgent.indexOf('Android')>=0)?true : false;
function logs(m){
    uexLog.sendLog(m);
}
 function isDefine(para) {
    if ( typeof para == 'undefined' || para == "" || para == "[]" || para == null || para == undefined || para == 'undefined'|| para == '[]')
        return false;
    else
        return true;
}
//获取年份//获取月份//获取日期
function NYR(date,flag){
    var da=new Date(date);
    if(isDefine(flag)){
        if(flag==1){
            return da.getFullYear()+"-"+((da.getMonth()+1)<10?"0"+(da.getMonth()+1):(da.getMonth()+1))+"-"+(da.getDate()<10?"0"+da.getDate():da.getDate());
        }else{
            return ((da.getMonth()+1)<10?"0"+(da.getMonth()+1):(da.getMonth()+1))+"-"+(da.getDate()<10?"0"+da.getDate():da.getDate());
        }
        
    }else{
        return da.getFullYear()+"年"+((da.getMonth()+1)<10?"0"+(da.getMonth()+1):(da.getMonth()+1))+"月"+(da.getDate()<10?"0"+da.getDate():da.getDate())+"日";
    }    
}
//获取小时//获取分
function HM(date){
    var da=new Date(date);
    var hour=da.getHours();
    if(hour<10){
        hour="0"+hour;
    }
    var minutes=da.getMinutes();
    if(minutes<10){
        minutes="0"+minutes;
    }
    return hour+":"+minutes
}

/**
 * 去除字符串中的空格
 * @param String
 */
function trim(str){ //删除左右两端的空格
    return str.replace(/(^\s*)|(\s*$)/g, "");
}

//截取字符串
function  subStr(str){
  var len=trim(str).length;  
  var s="";
   if(len>4){
       s=trim(str).substring(0,3);
       return s;  
   } else{
       s=str;
       return s; 
   }
}
//截取页面字符
function csubstr(str,s,e){
    var r='';
    if(typeof(str)!='string'){
        return str;
    }
    var l=str.length;
    if(e>l*2){
        return str;
    }
    for(var i=0;i<l;i++){
        if(s>i)continue;
        if(str.charCodeAt(i)>255){ //返回指定位置的Unicode
            if( e == 1 ){//剩一个字符
                r=r+"...";
                return r;
            }  
            e=e-2;
        }else{           
            e--;
        }
        if(str.charAt(i)==undefined){//防止取值溢出
            return r;
        }
        r=r+str.charAt(i);  //字符子串。
        if(e<=0){
            r=r+"...";
            return r;
        }
    }
    return r;
}

  function  getGroupId(node){//通过群列表中的id 获取群成员      注意 ：群列表是通过节点获取得到群id 
       var list= localStorage.getItem("groupList");
       var listObj = (JSON.parse(list)).msg.grouplist;
      // var groupNode = appcan.getLocVal("groupNode");    
       for(var i=0;i<listObj.length;i++){
           if((listObj[i].groupname).toLowerCase() ==node.toLowerCase()){             
               localStorage.setItem("groupId",listObj[i].groupid);  //群id
               var param = {
                    groupid:listObj[i].groupid,                    //  群ID
                    imId: localStorage.getItem("simcere.runtime.imUserId")                      //  用户ID
               }
              appcan.window.publish("E_GROUP_MEM_ROLE",param);
               break;
                    
           }else{
           }
       } 
}

function leftGroupHtml(dataList,selfImg1,i,flag,userId_name,copyId){
   //flag为0代表是聊天记录;1代表监听到好友发的新消息
    var flags = flag;
     if(flags == 0){
       //消息类型，1-文本；2-图片；3-语音；
        var datatype = dataList[i].msgType;
        //语音已读和未读的状态
        var voiceStatus = dataList[i].voiceStatus;
        var content = dataList[i].content;
        var fileUrl = dataList[i].resPath;
        var senderId = dataList[i].msgSender;
        var userName =userId_name[senderId].userName;
        var msgtimess = dataList[i].voiceTime;
        var msgId = dataList[i].msgId;
    } else {
        var datatype = dataList.msgType;
        var voiceStatus = 3;
        var msgId = dataList.msgId;
        var content = dataList.content;
        var fileUrl = dataList.resPath;
        var senderId = dataList.msgSender;
        var userName =userId_name[senderId].userName;
        var msgtimess = dataList.voiceTime;
        var msgtimess = Number(msgtimess);
        if (msgtimess <= 3) {
            var yywidth = 3.5;
        } else if (3 < msgtimess <= 60) {
            var yywidth = 3.5 + (msgtimess - 3) * 0.1;
        }
    }
    
   switch(parseInt(datatype)){
       
      case 1:
        var str = '<div class="ub hhuinn-tb ">' + 
                    '<div class="iconwh ub-img" style="background-image:url(' + selfImg1 + ') !important"></div>' + 
                    '<div class="ub-f1 ub hhkuinn-l ub-ver">'+
                        '<div class=" ub umar-b "><div class="ulev-1">'+userName+'</div></div>'+
                         '<div id="'+ copyId +'" class="ub-f1 ub">' +
                           '<div class="hhjiantou huihuajiantoul ub-img-r lft"></div>' + 
                           '<div class=" ub hhuinn bg-wh hhuc-a">'+
                             '<div id="'+ i +'" class="gz-ulev hhline-hei bc-text ub ub-ac umw2 umh3 hx copyMessage">' + content + '</div>'+
                                '</div>' +                               
                              '</div>' +
                            '</div>' +
                           '<div class="ub-f200"></div>'+                           
                         '</div>';  
       break;
     case 2:
        var str = '<div class="ub hhuinn-tb ">' +
                     '<div class="iconwh ub-img" style="background-image:url(' + selfImg1 + ') !important"></div>' +
                       '<div class="ub-f1 ub hhkuinn-l ub-ver">'+
                           '<div class=" ub umar-b ">'+
                                 '<div class="ulev-1">'+userName+'</div>'+
                            '</div>' + 
                           '<div class="ub-f1 ub">'+
                             '<div class="hhjiantou huihuajiantoul ub-img-r posiright lft"></div>' +
                             '<div class="ub piclist"  id="tp_'+msgId+'" tpsrc="'+fileUrl+'">' + 
                                  '<div class=" ub-img1 ub-f1 hhuc-a hhuba1 hhumaxwh1" style="background-image:url('+fileUrl+') !important;opacity: 0.4;display:block;" id="'+msgId+'">'+
                                      '<div class="ub ub-ac ub-pc ub-img huihuaunload1 bai" style="height:2em;margin-top:2.75em;color:#7B7777;"></div>'+
                                  '</div>' +
                           '</div>'+
                         '</div>' +
                       '</div>' + 
                     '<div class="ub-f200"></div>' + '</div>';                  
     break;
     
    case 3:
        switch (parseInt(voiceStatus)){
           case 1: 
             var str = '<div class="ub hhuinn-tb " >'+
                        '<div class="iconwh  ub-img" style="background-image:url(' + selfImg1 + ') !important"></div>' + 
                        '<div class="ub-f1 ub hhkuinn-l ub-ver">'+
                          '<div class=" ub umar-b ">'+
                            '<div class="ulev-1">'+userName+'</div></div>' +
                          '<div class=" ub duifshengyin audioPlay" id="'+msgId+'">' + 
                            '<div class="uhide dz" flag="0">' + fileUrl + '</div>' +
                            '<div class="uhide tian">' + msgId + '</div>' + 
                            '<div class="hhjiantou huihuajiantoul ub-img-r lft"></div>' + 
                            '<div class=" ub yuyinduihua hhuc-a bg-wh hhuinn" id="dddd' + i + '">' + 
                              '<div class="ub ub-ac">' + '<div class="yuyin ub-img"></div>' +
                              '<div id="yuyinlen" class="aaaaa gzt-green ulev-1 uinn4 " >' + msgtimess +
                               '</div>' + 
                            '</div>' + 
                            '<div class="gzt-green" style="width: 0.4375em;height: 0.4375em;">\'\'</div>' + 
                           '</div><div class="yuand ub-img"></div>' + 
                         '</div>'  + '</div>' + '<div class="ub-f200"></div>' + '</div>';
            
            break;
            
          case 2: 
             var str = '<div class="ub hhuinn-tb " >' +
                        '<div class="iconwh ub-img" style="background-image:url(' + selfImg1 + ') !important"></div>' +
                         '<div class="ub-f1 ub hhkuinn-l ub-ver">'+
                           '<div class=" ub umar-b ">'+
                            '<div class="ulev-1">'+userName+'</div></div>' + 
                            '<div class=" ub duifshengyin audioPlay" id="'+msgId+'">' + 
                            '<div class="uhide dz" flag="0">' + fileUrl + '</div>' +
                            '<div class="uhide tian">'+msgId+'</div>'+ 
                             '<div class="hhjiantou huihuajiantoul ub-img-r lft"></div>' + 
                             '<div class=" ub yuyinduihua hhuc-a bg-wh hhuinn" id="dddd' + i + '">' + 
                             '<div class="ub ub-ac">' + '<div class="yuyin ub-img"></div>' +
                              '<div id="yuyinlen" class="aaaaa gzt-green ulev-1 uinn4 " >' + msgtimess + '</div>' + '</div>' +
                               '<div class="gzt-green" style="width: 0.4375em;height: 0.4375em;">\'\'</div>' + '</div>' + '</div></div>' + 
                               '<div class="ub-f200"></div>' + '</div>';
            
            break;
            
          case 3: 
              var str = '<div class="ub hhuinn-tb " >' + 
                    '<div class="iconwh ub-img" style="background-image:url(' + selfImg1 + ') !important"></div>' + 
                    '<div class="ub-f1 ub hhkuinn-l ub-ver">'+
                         '<div class=" ub umar-b ">'+
                             '<div class="ulev-1">'+userName+'</div>'+                             
                          '</div>' + 
                    '<div class=" ub duifshengyin audioPlay" id="'+msgId+'">' + 
                       '<div class="uhide dz" flag="0">' + fileUrl+ '</div>' +
                       '<div class="uhide tian">'+msgId+'</div>'+ 
                       '<div class="hhjiantou huihuajiantoul ub-img-r lft"></div>' + 
                       '<div class=" ub yuyinduihua hhuc-a bg-wh hhuinn" style="width:'+yywidth+'em;">' + 
                          '<div class="ub ub-ac">' +
                              '<div class="yuyin ub-img"></div>' +
                              '<div id="yuyinlen" class="aaaaa gzt-green ulev-1 uinn4 gzuinn-l" >' + msgtimess + '</div>' +
                          '</div>' + 
                          '<div class="gzt-green" style="width: 0.4375em;height: 0.4375em;">\'\'</div>' +
                       '</div>' +
                       '<div class="yuand ub-img"></div>'+ '</div>'  + '</div>' + '<div class="ub-f200"></div>' + '</div>';                    
            
            break;    
            
             
            
            
        }
       
     break;
 
 
   }
    
 return str;   
    
}

function rightHtml(dataList,selfImg1,i){
  //消息类型，1-文本；2-图片；3-语音；
    var datatype = dataList[i].msgType;  
    var sendState= dataList[i].sendStatus;
    var msgId=dataList[i].msgId;
    switch(parseInt(datatype)){
        case 1:
          switch(parseInt(sendState)){
            case 2:
              var str = '<div class="ub hhuinn-tb ">' + 
                         '<div class="uhide tm">' + msgId + '</div>' +
                          '<div class="ub-f200"></div>' +
                         '<div id="copyMessage'+ i +'" class="ub-f1 ub hhkuinn-r ub-pe">' +
                           '<div class="ub">' + 
                             '<div class="ub-f1 ub hhuinn hhc-green hhuc-a">'+
                               '<div id="'+ i +'" class="gz-ulev hhline-hei bc-text ub ub-ac umw2 hx copyMessage">' + dataList[i].content + '</div></div>' +
                             '<div class="hhjiantou huihuajiantour ub-img-l posileft"></div>' + '</div>' + '</div>' + '<div class="iconwh ub-img" style="background-image:url(' + selfImg1 + ') !important"></div>' + '</div>';
            
            break;
            
            case 3:
             var str = '<div class="ub hhuinn-tb">' + 
                        '<div class="ub-f200"></div>' + 
                        '<div class=" iconwh1 huihuaunload ub-img"></div> ' +
                        '<div id="copyMessage'+ i +'" class="ub-f1 ub hhkuinn-r ub-pe">' +
                          '<div class="ub">' + 
                           '<div class="ub-f1 ub hhuinn hhc-green hhuc-a">'+
                             '<div id="'+ i +'" class="gz-ulev hhline-hei bc-text ub ub-ac umw2 hx copyMessage">' + dataList[i].content + '</div>'+
                           '</div>' +                             
                           '<div class="hhjiantou huihuajiantour ub-img-l posileft"></div>' +
                         '</div>' +
                       '</div>' + 
                      '<div class="iconwh ub-img" style="background-image:url(' + selfImg1 + ') !important"></div>' + '</div>';       
            break;                
            }
        break;
        
        case 2:
          switch(parseInt(sendState)){
            case 2:
              var str = '<div class="ub hhuinn-tb">' + 
                          '<div class="uhide tm">' + msgId + '</div>' +
                           '<div class="ub-f200"></div>' + 
                           '<div class="ub-f1 ub hhkuinn-r ub-pe">' + 
                             '<div class="ub-f1 ub ub-pe">' + 
                               '<div class="ub piclist" id="tp_'+msgId+'" tpsrc="'+dataList[i].resPath+'">' +
                                   '<div class="ub-img1 ub-f1 hhuc-a hhuba hhumaxwh1" style="background-image:url('+dataList[i].resPath+') !important" >'+
                                   '</div>' + 
                                '</div>' + 
                             '</div>' + 
                           '<div class="hhjiantou huihuajiantour ub-img-l posileft2"></div>' + '</div>' + '<div class="iconwh ub-img" style="background-image:url(' + selfImg1 + ') !important"></div>' + '</div>';
            break; 
            
            case 3:
              var str = '<div class="ub hhuinn-tb piclist">' +
                          '<div class="ub-f200"></div>' + 
                          '<div class=" iconwh1 huihuaunload ub-img"></div>' + 
                          '<div class="ub-f1 ub hhkuinn-r ub-pe">' +
                            '<div class="ub-f1 ub ub-pe">' + 
                               '<div class="ub piclist" id="tp_'+msgId+'" tpsrc="'+dataList[i].resPath+'">' + 
                                  '<div class="ub-img1 ub-f1 hhuc-a hhuba hhumaxwh1" style="background-image:url('+dataList[i].resPath+') !important" id="img1"></div>' + '</div>' + '</div>' + '<div class="hhjiantou huihuajiantour ub-img-l posileft2"></div>' + '</div>' + '<div class="iconwh ub-img" style="background-image:url(' + selfImg1 + ') !important"></div>' + '</div>';
            break;   
              
              
          }
        break;
        
        case 3:        
          switch(parseInt(sendState)){
            case 2:
               var str = '<div class="ub hhuinn-tb">' +
                          '<div class="uhide tm">' + msgId + '</div>' +                         
                           '<div class="ub-f200"></div>' + 
                             '<div class="ub-f1 ub hhkuinn-r ub-pe">' +
                               '<div class=" ub-img"></div>' +
                                '<div class=" ub myshengyin audioPlay">' +
                                  '<div class="uhide dz" flag="0">' + dataList[i].resPath + '</div>' + 
                                '<div class="ub-f1 ub yuyinduihua hhuc-a  ub-pe hhc-green hhuinn voice" id="dddd' + i + '" >' + 
                                 '<div class="ub ub-ac">' + 
                                  '<div id="yuyinlen" class="bc-text-head ulev-1 uinn4">' + dataList[i].voiceTime + '</div>' +                               
                                   '</div> ' + 
                                   '<div class="yuyin3 ub-img"></div>' + 
                                    '<div class="ub ub-ac rwagzmbuinn-l1">' + 
                                    '<div class="yuyin2 ub-img"></div>' + '</div>' + '</div> ' + '<div class="hhjiantou huihuajiantour2 ub-img-l posileft"></div>' + '</div>' + '</div>' + '<div class="iconwh ub-img" style="background-image:url(' + selfImg1 + ') !important"></div>' + '</div>';
            break; 
            
            case 3:
              var str = '<div class="ub hhuinn-tb ">' + 
                          '<div class="ub-f200"></div>' + 
                          '<div class=" iconwh1 huihuaunload ub-img"></div>' +
                           '<div class="ub-f1 ub hhkuinn-r ub-pe">' + 
                            '<div class="ub-img"></div>' +
                              '<div class=" ub myshengyin ">' +
                                '<div class="ub-f1 ub yuyinduihua hhuc-a  ub-pe hhc-green hhuinn voice" id="dddd' + i + '">' + 
                                 '<div class="ub ub-ac">' + 
                                  '<div id="yuyinlen" class="bc-text-head ulev-1 uinn4">' + dataList[i].voiceTime + '</div>' + '</div> ' + '<div class="yuyin3 ub-img"></div>' + '<div class="ub ub-ac rwagzmbuinn-l1">' + '<div class="yuyin2 ub-img"></div>' + '</div>' + '</div> ' + '<div class="hhjiantou huihuajiantour2 ub-img-l posileft"></div>' + '</div>' + '</div>' + '<div class="iconwh ub-img" style="background-image:url(' + selfImg1 + ') !important"></div>' + '</div>';
            break;   
                   }
        
        break;
           
    }
    
 return str;   
}
//发送成功页面滚到最底部
function toDown(){
  window.scrollTo(0, document.body.scrollHeight);
}

function replaceStr(str){ 
  var s=str.replace(/"|'/g,'”');  
  return s;
}
function getNameById(id){
    var userId_name=localStorage.getItem("simcere.im.userId_name");
    if(userId_name == null || userId_name==""){
        userId_name ={};
    }else{
       userId_name = JSON.parse(localStorage.getItem("simcere.im.userId_name"));
    }
    var url = SimcereConfig.server.emm+'/appIn/queryUser/'+id; 
    appcan.request.ajax({
    url : url,
    type : 'GET',
    offline : true,
    dataType : "text" ,
    success : function(data) {
        var data=JSON.parse(data);
        if(data.status=="ok"){
            userId_name[data.userId]={
                userName : data.userName
            } 
           var name = data.userName ; 
           localStorage.setItem("simcere.im.userId_name",JSON.stringify(userId_name));
           return name ; 
        }    
     }
    })    
}


