/**
 * Created by mitty on 15/11/25.
 */
var SimcereConfig = {
    "appId": "aaaio10008",
    "appKey": "f617f351-308f-4b1c-b7e5-7344f9ec0026",
    "widgets": {
        "Contacts": "aaaio10007",
        "NN": "aaaio10010",
        "BBS": "aaaio10012",
        "KHGL": "aaaio10021",
        "SO": "aaaio10020",
        "Contract": "aaaio10019"
    },
    //�?要预安装的app
    preInstallApps: [
        'aaaio10007',
        'aaaio10010',
        'aaaio10012'
    ],
    "server": {
        "emm": "https://modev.isimcere.com:18443",
        "mas": "http://modev.isimcere.com:9999",
        "bbs": "http://58.240.92.222:8081/source/plugin/zywx/rpc/"
    },
    "ui": {
        "toastDuration": 2000
    },
    "emmLoginDomain": "simcere"
};