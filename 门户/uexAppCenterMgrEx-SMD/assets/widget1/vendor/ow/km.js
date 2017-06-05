/**
 * Created by mitty on 15/12/17.
 */

/**
 * api测试工具
 * unix timestamp
 * @param counter
 * @param uts
 */
function apiTest(counter, uts){
    var params = {
        "userId": '13205',
        "pageNumber": 1,
        "pageSize": 20,
        "rangeId": "4"
    };

    var opt = {
        timeout: 5000,
        url: SimcereConfig.server.mas + '/message/ScrQueryMessages'
    };

    appcan.request.ajax({
        type: 'POST',
        url: opt.url,
        data: JSON.stringify(params),
        contentType: 'application/json',
        dataType: 'json',
        timeout: opt.timeout,
        success: function (data, status, xhr) {
            apiTest.resHandler(counter, 'zy', Date.now()-uts, 0);
        },
        error: function (xhr, errorType, error) {
            apiTest.resHandler(counter, 'zy', Date.now()-uts, -1);
        }
    });

    $.ajax({
        type: 'POST',
        url: opt.url,
        data: JSON.stringify(params),
        contentType: 'application/json',
        dataType: 'json',
        timeout: opt.timeout,
        success: function (data) {
            apiTest.resHandler(counter, 'jq', Date.now()-uts, 0);
        },
        error: function () {
            apiTest.resHandler(counter, 'jq', Date.now()-uts, -1);
        }
    });
}
//结果处理
apiTest.resHandler = function (counter, method, uts, isError) {
    var tr = document.createElement('tr');
    tr.innerHTML = [
        '<td>' + counter + '</td>',
        '<td>' + method + '</td>',
        '<td>' + uts + '</td>',
        '<td>' + (isError ? 'fail' : 'ok') + '</td>'
    ].join('');

    apiTest.tb.insertBefore(tr, apiTest.tb.firstElementChild.nextElementSibling);
};

apiTest.init = function () {
    apiTest.tb = (function () {
        var table = document.createElement('table');
        table.innerHTML = [
            '<tr>',
            '    <th>No.</th>',
            '    <th>Agent</th>',
            '    <th>Cost</th>',
            '    <th>Result</th>',
            '</tr>'
        ].join('');
        table.style.position='absolute';
        table.style.left='0px';
        table.style.top='0px';

        document.body.appendChild(table);
        return table;
    })();

    var counter = 0;
    setInterval(function () {
        apiTest(counter++, Date.now());
    }, 1000);
};