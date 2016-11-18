function sogouSugg(){
    if(!window.JSON){var f=new Object();window.JSON=f;f.stringify=f.stringify?f.stringify:(function(){var i=/[\u0000\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,l=/[\\\"\x00-\x1f\x7f-\x9f\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,m,h,o={'\b':"\\b","\t":"\\t","\n":"\\n","\f":"\\f","\r":"\\r",'"':'\\"',"\\":"\\\\"},n;function g(p){l.lastIndex=0;return l.test(p)?'"'+p.replace(l,function(q){var r=o[q];return typeof r==="string"?r:"\\u"+("0000"+q.charCodeAt(0).toString(16)).slice(-4)})+'"':'"'+p+'"'}function k(x,t){var r,q,y,p,u=m,s,w=t[x];if(w&&typeof w==="object"&&typeof w.toJSON==="function"){w=w.toJSON(x)}if(typeof n==="function"){w=n.call(t,x,w)}switch(typeof w){case"string":return g(w);case"number":return isFinite(w)?String(w):"null";case"boolean":case"null":return String(w);case"object":if(!w){return"null"}m+=h;s=[];if(Object.prototype.toString.apply(w)==="[object Array]"){p=w.length;for(r=0;r<p;r+=1){s[r]=k(r,w)||"null"}y=s.length===0?"[]":m?"[\n"+m+s.join(",\n"+m)+"\n"+u+"]":"["+s.join(",")+"]";m=u;return y}if(n&&typeof n==="object"){p=n.length;for(r=0;r<p;r+=1){q=n[r];if(typeof q==="string"){y=k(q,w);if(y){s.push(g(q)+(m?": ":":")+y)}}}}else{for(q in w){if(Object.hasOwnProperty.call(w,q)){y=k(q,w);if(y){s.push(g(q)+(m?": ":":")+y)}}}}y=s.length===0?"{}":m?"{\n"+m+s.join(",\n"+m)+"\n"+u+"}":"{"+s.join(",")+"}";m=u;return y}}var j=function(s,q,r){var p;m="";h="";if(typeof r==="number"){for(p=0;p<r;p+=1){h+=" "}}else{if(typeof r==="string"){h=r}}n=q;if(q&&typeof q!=="function"&&(typeof q!=="object"||typeof q.length!=="number")){throw new Error("JSON.stringify")}return k("",{"":s})};return window.JSON&&window.JSON.stringify?window.JSON.stringify:j})();f.parse=function(h){if(h){try{return window.JSON&&window.JSON.parse?window.JSON.parse(h):(new Function("return "+h))()}catch(g){return null}}else{return null}}}
    function $c(n){return document.createElement(n)}
    function strip(Q){return Q.replace(/(^[\s\t\xa0\u3000]+)|([\u3000\xa0\s\t]+x24)/g,"").replace(/[\u2006]/g,"")}
    function bind(elem, evt, func){
        if (elem){
            return elem.addEventListener?elem.addEventListener(evt,func,false):elem.attachEvent("on"+evt,func);
        }
    }
    function stopEvent(c){c=c||window.event;if(!c)return;c.preventDefault();c.stopPropagation()}
    function J(E) {
        var C = 0;
        if (E.offsetParent) {
            for (; ;) {
                C += E.offsetTop;
                if (!E.offsetParent) {
                    break
                }
                E = E.offsetParent
            }
        } else {
            if (E.y) {
                C += E.y
            }
        }
        return C
    }
    function htmlencode(s){
        var div = $c('div');
        div.appendChild(document.createTextNode(s));
        return div.innerHTML;
    }
    function htmldecode(s){
        var div = $c('div');
        div.innerHTML = s;
        return div.innerText || div.textContent;
    }
    var that = this,sugid,inputid,clearid,boxid,submitform,uri,sug_status,sugdata,input_elem,userInputString="",firstload;

    that.init = function(config){
        sugid=config.sugid,
            inputid=config.inputid,
            input_elem = $("#"+inputid);
        
            clearid=config.clearid,
            overlayid = config.overlayid,
            boxid=config.boxid,
            submitform=config.submitform;
        
        //alert(uri+"ffffff")
        uri=config.uri||"/app/getSuggestion";  
        
        firstload=config.firstload

        sugdata = $("#"+boxid);
        

        /** sugg **/
        var sugname = "sug";
        //<li class="search-suggestion-clear">清除搜索历史</li>
        var suggTail = '<li class="search-suggestion-clear" onclick="window.'+sugname+'.clearLocalStorage();">清除搜索历史</li>';
        var info={inputId:inputid,sugNum:5,sugScriptId:"sug",wrapper:boxid,form:submitform,resetbtn:clearid,sugg:overlayid};
        var sug = window[sugname]={};
        sug_status={focus:false,lastKw:"",cache:{},isRequesting:false,currentSugKw:"",isClose:0,lastHTML:"",toucheditem:null};

        function createWrapper(){
            sug_status.lastKw=$(info.inputId).value;
            sug_status.focus=false;
            addListener()
        }
        function s_ajax(value){
        	
        	if (value=='') {
        		//加载本地搜索记录5条
                var local_nl = localStorage.getItem('local_nl')==undefined?'':localStorage.getItem('local_nl');
                var arr_nl = local_nl.split('|');
                if (arr_nl.length>0 && arr_nl[0]!='') {
                	paint_result({list:arr_nl.length<=5?arr_nl:arr_nl.slice(arr_nl.length-5)});
                }
        	} else {
        		$.ajax({
                    type:"get",
                    url:uri,
                    dataType:"json",
                    async:false,
                    data:{query:value},
                    success: function (data) {
                        if(data.list.length>0) {
                        	paint_result(data);
                        }else{
                            displaynone()
                        }
                    }
                });
        	}
            
        }
        function itemontouchstart(that){
            var touched = sug_status.toucheditem;
            if(touched)touched.className="";
            sug_status.toucheditem = that
            that.className="touched";
        }
        function paint_result(W){
            var tmp=[];
            if(inputid == "query" && (W.query==undefined || W.query==null)){
            	$(".suggestion").hide();
            }
            else{
            	if(W.query==undefined || W.query==null){
	                //tmp.push('<li style="line-height:23px;font-size:12px;color:#ccc;background-color:#F5F5F6;border-bottom:0">热搜词</li>');
		        }
	            
	            for(var T=0;T<W.list.length&&T<info.sugNum;T++){
	                var Q=W.list[T];
	                Q=Q.toLowerCase();
	                if(Q.length>15){
	                    Q = Q.substring(0,15)
	                }
	                var U=new RegExp(W.query+"(.*)");
	                var S=U.exec(Q);
	                var V="";
	                if(S){
	                    V=W.query+"<span>"+S[1]+"</span>"
	                }else{
	                    V=""+Q+""
	                }
	                if(Q != W.query){
	                    tmp.push('<li ontouchstart="window.'+sugname+'.itemontouchstart(this)" onclick="window.'+sugname+'.select(\''+escape(Q)+'\','+(T+1)+',event);"><a href="javascript:" onclick="javascript:window.'+sugname+'.add(\''+escape(Q)+'\','+(T+1)+',event);">'+V+'</a><i class="search-suggestion-query"></i></li>')
	                }
	            }
            }
            
            if(W.query == undefined){
                tmp.push(suggTail);
            }
            var html = "<ul>"+tmp.join("")+"</ul>";
            if(tmp.length>0) {
            	display(html);
            }
        }

        function loadSuggestion(){
        	sug_status.focus=true;
            var uaTest=navigator.userAgent;
            var isIpad=uaTest.match(/(iPad).*OS\s([\d_]+)/);
            var isIphone=!isIpad && uaTest.match(/(iPhone\sOS)\s([\d_]+)/);
            var isIos=isIpad||isIphone;
            if(isIos){
                setTimeout(function(){window.scrollTo(0,J($("#"+info.inputId)[0])-5)},10)
                setTimeout(L,10)
            }else{
                setTimeout(function(){window.scrollTo(0,J($("#"+info.inputId)[0])-5)},1)
                setTimeout(L,100)
            }
            
        }

        function addListener(){
        	
            bind($("#"+info.inputId)[0],"input",function(Q){
                loadSuggestion()
            },false);
            bind($("#"+info.inputId)[0],"focus",function(){
                loadSuggestion()
            },false);
            bind($("#"+info.inputId)[0],"blur",function(Q){/*sug_status.focus=false;setTimeout(displaynone,500)*/},false)
            bind($("#"+info.resetbtn)[0],"click",function(){
            	$("#"+info.inputId).val("");
            },false);
            var form=info.form;
            bind(document.body,"click",function(event){
                event = event||window.event
                if(event){
                    if(event.target!=$("#"+info.inputId)[0]){
                        sug_status.focus = false;
                        displaynone();
                    }
                }
            },false)

            bind(document.body,"touchend",function(e){
                var touched = sug_status.toucheditem;
                if(touched){
                    touched.className="";
                }
            });

        }
        function displaynone(){
            $("#"+info.wrapper).hide();
            $("#"+info.sugg).hide();
        }
        function display(html){
            $("#"+info.wrapper).html(html);
            $("#"+info.wrapper).show();
            $("#"+info.sugg).show();
        }
        function reset(k){
            var e = $(info.resetbtn)
            if(e){
                e.style.display=k;
            }
        }
        function L(){
            if(sug_status.isClose==1){return }
            if(sug_status.focus){
                var Q=$("#"+info.inputId).val();
                Q=Q.toLowerCase();
                Q=strip(Q);
                userInputString=Q;
                s_ajax(Q)
            }
        }
        function search(R,pos,event){
            R = unescape(R);
            stopEvent(event);
            var form=info.form;
            $("#"+info.inputId).val(R);
            setTimeout(function(){sug_status.focus=false;displaynone();form.submit();},200);
        }
        function add(R,pos,event){
            R = unescape(R);
            stopEvent(event)
            var form=info.form;
            $("#"+info.inputId).val(R);
            loadSuggestion();
            //check 
            search(R,pos,event);
        }
        sug.close=function(){
            displaynone();
            sug_status.focus=false;
        };
        sug.clearLocalStorage = function () {
        	localStorage.removeItem('local_nl');
        };
        sug.select=search
        sug.add=add
        sug.clear=function(){
            displaynone();
        };
        sug.itemontouchstart = itemontouchstart;
        createWrapper();
        if (firstload){
            loadSuggestion();
        }

    };
};