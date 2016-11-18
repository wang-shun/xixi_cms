/**
 * based on zepto.js
 */

/*
 * simple util
 */

(function(window) {

    window.host = location.protocol+'//'+ location.host;

    //tracy(2014-12-27): 取url的param
    window.param = function(name) {
        name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
        var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),//'/'在'[]'里面可以不转义
            results = regex.exec(location.search);
        return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
    };

    String.prototype.trim = function(){
        return this.replace(/^\s+|\s+$/g,'');
    };
    String.prototype.startsWith = function(target){
        return this.slice(0, target.length) == target;
    };

    Date.prototype.Format = function(fmt)
    { //author: meizz
        var o = {
            "M+" : this.getMonth()+1,                 //月份
            "d+" : this.getDate(),                    //日
            "h+" : this.getHours(),                   //小时
            "m+" : this.getMinutes(),                 //分
            "s+" : this.getSeconds(),                 //秒
            "q+" : Math.floor((this.getMonth()+3)/3), //季度
            "S"  : this.getMilliseconds()             //毫秒
        };
        if(/(y+)/.test(fmt))
            fmt=fmt.replace(RegExp.$1, (this.getFullYear()+"").substr(4 - RegExp.$1.length));
        for(var k in o)
            if(new RegExp("("+ k +")").test(fmt))
                fmt = fmt.replace(RegExp.$1, (RegExp.$1.length==1) ? (o[k]) : (("00"+ o[k]).substr((""+ o[k]).length)));
        return fmt;
    }

    //tracy(2014-10-22): cookie
    window.CookieUtil = {
        get: function(cookieName) {
            var re = new RegExp("\\b" + cookieName + "=([^;]*)\\b");
            var arr = re.exec(document.cookie);
            return arr ? decodeURIComponent(arr[1]) : null;
        },

        set: function(name, value){
            var argv = arguments,
                argc = arguments.length,
                expires = (argc > 2) ? argv[2] : new Date(new Date().valueOf() + 365*24*60*60*1000),
                path = (argc > 3) ? argv[3] : '/',
                domain = (argc > 4) ? argv[4] : null,
                secure = (argc > 5) ? argv[5] : false;

            document.cookie = name + "=" + encodeURIComponent(value) + ((expires === null) ? "" : ("; expires=" + expires.toGMTString())) + ((path === null) ? "" : ("; path=" + path)) + ((domain === null) ? "" : ("; domain=" + domain)) + ((secure === true) ? "; secure" : "");
        },

        remove: function(name, path, domain) {
            if(this.get(name)){
                path = path || '/';
                document.cookie = name + '=' + '; expires=Thu, 01-Jan-70 00:00:01 GMT; path=' + path + (domain ? ('; domain=' + domain) : '');
            }
        }
    };

    window.NumberUtil = {
        toInt:function(val,defVal){
            defVal = defVal || 0;
            try{
                return parseInt(val,10);
            }catch (ex){
                return defVal;
            }
        }
    }


    //tracy(2014-10-22): date
    window.DateUtil = {
        //demo:20141022
        ymd:function(){
            return new Date().Format('yyyyMMdd');
        },
        //timeStamp
        tm:function(){
            return new Date().getTime();
        }
    }

    window.hasEvent = (function() {

        // Detect whether event support can be detected via `in`. Test on a DOM element
        // using the "blur" event b/c it should always exist. bit.ly/event-detection
        var needsFallback = !('onblur' in document.documentElement);

        function inner(eventName, element) {

            var isSupported;
            if (!eventName) { return false; }
            if (!element || typeof element === 'string') {
                element = createElement(element || 'div');
            }

            // Testing via the `in` operator is sufficient for modern browsers and IE.
            // When using `setAttribute`, IE skips "unload", WebKit skips "unload" and
            // "resize", whereas `in` "catches" those.
            eventName = 'on' + eventName;
            isSupported = eventName in element;

            // Fallback technique for old Firefox - bit.ly/event-detection
            if (!isSupported && needsFallback) {
                if (!element.setAttribute) {
                    // Switch to generic element if it lacks `setAttribute`.
                    // It could be the `document`, `window`, or something else.
                    element = createElement('div');
                }

                element.setAttribute(eventName, '');
                isSupported = typeof element[eventName] === 'function';

                if (element[eventName] !== undefined) {
                    // If property was created, "remove it" by setting value to `undefined`.
                    element[eventName] = undefined;
                }
                element.removeAttribute(eventName);
            }

            return isSupported;
        }
        return inner;
    })();

    function createElement() {
        var isSVG = document.documentElement.nodeName.toLowerCase() === 'svg';
        if (typeof document.createElement !== 'function') {
            // This is the case in IE7, where the type of createElement is "object".
            // For this reason, we cannot call apply() as Object is not a Function.
            return document.createElement(arguments[0]);
        } else if (isSVG) {
            return document.createElementNS.call(document, 'http://www.w3.org/2000/svg', arguments[0]);
        } else {
            return document.createElement.apply(document, arguments);
        }
    }

})(window);

/*
 *  handle error
 */
(function(window){
    window.errImgs = [];
    window.onerror = function(msg,url,num){
        var len = errImgs.length;
        errImgs[len]= new Image();
        errImgs[len].src=window.host +'/h5/images/tj.gif?type=geriPbError&msg='+encodeURIComponent(msg)+'&url='+encodeURIComponent(url)+'&num='+num+'&t='+(new Date()).getTime();
        //tracy(2014-12-27):线上不报错
        if(location.host=='yd.sogou.com')
            return true;
    };
})(window);

/*
 * ping back util
 */

(function(window){
    window.pbArr = [];
    var pbPage = location.pathname ? location.pathname.substring(1).replace(/\//g,'_'):'unknown';
    var pbPrefix = window.host + '/h5/images/tj.gif?t=';
    var Stats = {
        pb:function(ele){
            try{
                var tmp=[pbPrefix, (new Date()).getTime(), "&type=cl&page=", encodeURIComponent(pbPage)];
                var attributes = ele.attributes;
                //tracy(2015-1-5): basic attr id,key
                var id = attributes['id'];
                if(id){
                    tmp.push('&id=');
                    tmp.push(encodeURIComponent(id.value));
                }
                var key = attributes['key'];
                if(key){
                    tmp.push('&key=');
                    tmp.push(encodeURIComponent(key.value));
                }

                //tracy(2015-1-5): additional attr startsWith 'geri-pb-'
                for(var i= 0,len=attributes.length;i<len;i++){
                    var attr_name = attributes[i].name;
                    if(attr_name.startsWith('geri-pb-')){
                        var attr = attributes[attr_name];
                        if(attr){
                            tmp.push('&');
                            //remove 'geri-pb-'
                            tmp.push(attr_name.substring(8));
                            tmp.push('=');
                            tmp.push(encodeURIComponent(attr.value));
                        }
                    }
                }

                tmp.push('&');
                tmp.push(window.gp);

                var pb_arr_len = pbArr.length;
                pbArr[pb_arr_len]=new Image();
                pbArr[pb_arr_len].src=tmp.join('');
            }catch(e){
                console.log(e);
            }
        },
        //tracy(2016-07-14):增加一个更通用的pingBack方法common ping back,统计非点击事件
        cpb:function(){
            try{
                var tmp=[pbPrefix, (new Date()).getTime(), "&type=cl&page=", encodeURIComponent(pbPage)];
                var length = arguments.length;
                if(length%2!=0||length<1){
                    return;
                }
                for(var i=0;i<length-1;i+=2){
                    tmp.push('&');
                    tmp.push(arguments[i]);
                    tmp.push('=')
                    tmp.push(encodeURIComponent(arguments[i+1]));
                }

                tmp.push('&');
                tmp.push(window.gp);

                var pb_arr_len = pbArr.length;
                pbArr[pb_arr_len]=new Image();
                pbArr[pb_arr_len].src=tmp.join('');
            }catch(e){
                console.log(e);
            }
        },
        pv:function(){
            var pb_arr_len = pbArr.length;
            pbArr[pb_arr_len]=new Image();
            var _gp = window.gp.replace(/gf=([^&]+)/,'gf='+param('gf'));
            pbArr[pb_arr_len].src=[pbPrefix, (new Date()).getTime(), '&type=pv&page=', encodeURIComponent(pbPage),'&',_gp].join("");
        },
        setPbPage:function(val){
            pbPage = val;
        }
    };

    window.Stats = Stats;
})(window);

//init
(function(window){
    window.onload = function(){
        document.addEventListener('click',function(event){
            event = event ? event : window.event;
            var target = event.target || event.srcElement;
            if(target&&target.nodeName=='HTML')
                return;
            //stop when bubble to body
            while(target && target.nodeName!='BODY'){
                if((target.nodeName=='A' || target.getAttribute('stats')=='1') && target.getAttribute('id')){
                    Stats.pb(target);
                    return;
                }else{
                    target = target.parentNode;
                }
            }

        },false);
    }
})(window);


/**
 * tip util
 */
(function (window) {
    var GeriTip = {
        toastElement:function($ele,intVal){
            intVal = intVal || 1000;
            if($ele.css('display')!='none')
                return;
            $ele.show();
            setTimeout(function(){
                $ele.hide();
            },intVal);
        },
        toast:function(tip,intVal){
            intVal = intVal || 1000;
            if($('#Geri-toast').length==0){
                $('body').prepend('<div id="Geri-toast" style="display: none;position: fixed;left: 50%;top: 50%;-webkit-transform: translate(-50%,-50%);transform: translate(-50%,-50%);width: 280px;text-align: center;font-size: 16px;line-height: 26px;padding: 15px 20px;color: #fff;background: rgba(51,51,51,.9);border-radius: 5px;z-index: 999;"></div>');
            }
            var $toast = $('#Geri-toast');
            if($toast.css('display')!='none')
                return;
            $toast.html(tip);
            $toast.show();
            setTimeout(function(){
                $toast.hide();
            },intVal);
        }
    }
    window.GeriTip = GeriTip;
})(window);



function fmtStr(str,length){
    if(!str)
        return '';
    if(str.length<length)
        return str;
    return str.substring(0,length-2)+'...';
}

function escape2Html(str) {
 var arrEntities={'lt':'<','gt':'>','nbsp':' ','amp':'&','quot':'"'};
 return str.replace(/&(lt|gt|nbsp|amp|quot);/ig,function(all,t){return arrEntities[t];});
}

//tracy(2015-2-25): max retry once
function getGeriText(url, callback, retry){
    $.ajax({
        type:'GET',
        url:url,
        success: function(text) {
            if(callback) {
                callback(text);
            }
        },
        error:function(xhr, type) {
            if(retry){
                getGeriText(url, callback);
            }else{
                callback(null);
            }
        }
    });
}

//tracy(2015-2-25): max retry once
function getGeriJson(url, callback, retry){
    $.ajax({
        type:'GET',
        url:url,
        dataType:'json',
        success: function(json) {
            if(callback) {
                callback(json);
            }
        },
        error:function(xhr, type) {
            if(retry){
                getGeriJson(url, callback);
            }else{
                callback(null);
            }
        }
    });
}

function postGeriJson(url, data, callback, retry){
    $.ajax({
        type:'POST',
        url:url,
        data:data,
        dataType:'json',
        success: function(json) {
            if(callback) {
                callback(json);
            }
        },
        error:function(xhr, type) {
            if(retry){
                postGeriJson(url, data, callback);
            }else{
                callback(null);
            }

        }
    });
}

// 2016-6-22 15:35:52 增加post支持json格式的数据，方便spring的@RequestBody
function postJsonGeriJson(url, data, callback, retry){
    $.ajax({
        type:'POST',
        url:url,
        data:JSON.stringify(data),
        dataType:'json',
        contentType: "application/json; charset=utf-8",
        success: function(json) {
            if(callback) {
                callback(json);
            }
        },
        error:function(xhr, type) {
            if(retry){
                postJsonGeriJson(url, data, callback);
            }else{
                callback(null);
            }

        }
    });
}

//js获取倒计时时间
function getCountDown(end) {
	var time = [];
	var diff = end-new Date();
	time[0] = parseInt(diff/1000/3600/24);
	time[1] = parseInt(diff/1000/3600) - time[0]*24;
	time[2] = parseInt(parseInt(diff/1000) % 3600/60);
	time[3] = parseInt(diff/1000) % 60;
	return time;
}

//tracy(2016-04-13):倒计时模块
function countDown(time){
    var d = time.eq(0), h = time.eq(1), m = time.eq(2), s = time.eq(3);
    var f_d = d.html(), f_h = h.html(), f_m = m.html(), f_s = s.html();
    if(f_s>0){
        s.html(f_s-1);
    }else if(f_s == 0 && f_m > 0){
        s.html(59);
        m.html(f_m-1);
    }else if(f_s == 0 && f_m == 0 && f_h > 0){
        s.html(59);
        m.html(59);
        h.html(f_h-1);
    }else if(f_s == 0 && f_m == 0 && f_h == 0 && f_d > 0){
        s.html(59);
        m.html(59);
        h.html(59);
        d.html(f_d-1);
    }
}

//tracy(2016-04-13):广播的轮播效果
function simpleSwipe($container,elements,_interval,_height){
    var interval = _interval || 3000;
    var length = $(elements).length;
    var height = _height || 39;
    if(length==2){
        $container.append($container.html());
        length=4;
    }
    var $elements = $(elements);
    if(length>2){
        var cur=0,after=1;
        setInterval(function(){
            for(var i=0;i<length;i++){
                if(i==cur){
                    $elements.eq(i).css('-webkit-transition','500ms');
                    $elements.eq(i).css('-webkit-transform','translate(0,-'+(i+1)*height+'px) translateZ(0)');
                }else if(i==after){
                    $elements.eq(i).css('-webkit-transition','500ms');
                    $elements.eq(i).css('-webkit-transform','translate(0,-'+i*height+'px) translateZ(0)');
                }else{
                    $elements.eq(i).css('-webkit-transition','0ms');
                    $elements.eq(i).css('-webkit-transform','translate(0,'+height+'px) translateZ(0)');
                }
            }
            cur = after;
            after = (after+1)%length;
        },interval);
    }
}
// 浏览器唤起app
function openApp(openUrl, callback) {
    //检查app是否打开
    function checkOpen(cb){
        var _clickTime = +(new Date());
        function check(elsTime) {
            if ( elsTime > 3000 || document.hidden || document.webkitHidden) {
                cb(1);
            } else {
                cb(0);
            }
        }
        //启动间隔20ms运行的定时器，并检测累计消耗时间是否超过3000ms，超过则结束
        var _count = 0, intHandle;
        intHandle = setInterval(function(){
            _count++;
            var elsTime = +(new Date()) - _clickTime;
            if (_count>=100 || elsTime > 3000 ) {
                clearInterval(intHandle);
                check(elsTime);
            }
        }, 20);
    }

    //在iframe 中打开APP
    var ifr = document.createElement('iframe');
    ifr.src = openUrl;
    ifr.style.display = 'none';
    if (callback) {
        checkOpen(function(opened){
            callback && callback(opened);
        });
    }

    document.body.appendChild(ifr);
    setTimeout(function() {
        document.body.removeChild(ifr);
    }, 2000);
}

//获取url参数
function getUrlParam(name,url) {
    var reg = new RegExp("(\\?|&)" + name + "=([^&]*)(&|$)");
    if(!url)  url =  window.location.search;
    var r = url.match(reg);
    if (r != null) return decodeURIComponent(r[2]); return null;
}

//修改、添加url参数
function setUrlParam(name,value,url){
    if(!url)  url =  window.location.search;
    if(url.indexOf('?') > -1){
        var p = new RegExp("(\\?|&)"+name+"=([^&]*)(&|$)");
        if(p.test(url)){
            url = url.replace(p,"$1"+name+"="+value+"$3");
        }else{
            url = url+'&'+name+'='+value;
        }
    }else{
        url = url+'?'+name+'='+value;
    }
    return url;
}

//判断是否在微信中打开  && 判断微信版本是否高于5.0:
//return 0-weixin < 5.0 ; 1-weixin>=5.0 ;-1 - 非weixin 
function is_weixin() {
	var wechatInfo = navigator.userAgent.match(/MicroMessenger\/([\d\.]+)/i);
	if( !wechatInfo ) {
		return -1;
	} else {
		if ( wechatInfo[1] < "5.0" ) {
			return 0;
		} else {
			return 1;
		}
	}
}

(function ($) {
    $.extend($, {
        throttle: function(delay, fn, debounce_mode) {
            var last = 0,
                timeId;

            if (typeof fn !== 'function') {
                debounce_mode = fn;
                fn = delay;
                delay = 250;
            }

            function wrapper() {
                var that = this,
                    period = Date.now() - last,
                    args = arguments;

                function exec() {
                    last = Date.now();
                    fn.apply(that, args);
                };

                function clear() {
                    timeId = undefined;
                };

                if (debounce_mode && !timeId) {
                    // debounce模式 && 第一次调用
                    exec();
                }

                timeId && clearTimeout(timeId);
                if (debounce_mode === undefined && period > delay) {
                    // throttle, 执行到了delay时间
                    exec();
                } else {
                    // debounce, 如果是start就clearTimeout.
                    timeId = setTimeout(debounce_mode ? clear : exec, debounce_mode === undefined ? delay - period : delay);
                }
            };
            // for event bind | unbind
            wrapper._zid = fn._zid = fn._zid || $.proxy(fn)._zid;
            return wrapper;
        },

        debounce: function(delay, fn, t) {
            return fn === undefined ? $.throttle(250, delay, false) : $.throttle(delay, fn, t === undefined ? false : t !== false);
        }
    });



})(Zepto);

//tracy(2016-05-23):prevent dns hijacking
top.tlbsEmbed=true;

