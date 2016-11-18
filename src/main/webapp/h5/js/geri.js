
var Geri = {

    toCopyright:function(ele){
        var bkey = $(ele).attr('bkey');
        location.href='/h5/cpt/detail?bkey='+bkey+'&'+ gp;
    },
    
    toPiracyBook:function(ele) {
    	var id = $(ele).attr('bid');
    	var md = $(ele).attr('md');
    	
    	location.href = 'http://k.sogou.com/touch/list?v=5&md='+md+'&id='+id+'&'+ gp;
    },
    
    toSubject:function(ele){
        var url = $(ele).attr('link');
        if($(ele).attr('type')=='3'){
            location.href=url+'&'+gp;
        }else{
            location.href=url;
        }
    },
    toLink:function(ele){
        var url = $(ele).attr('link');
        location.href=url;
    },
    loadSrc:function(url,id){
        var g = document.createElement('script'),s = document.getElementsByTagName('script')[0];
        g.type = 'text/javascript';g.async = true;g.src = url;g.id=id;g.charset="utf-8"
        s.parentNode.insertBefore(g, s);
    },
    toChapter:function(ele) {
    	location.href = "/h5/cpt/chapter?bkey="+$(ele).attr('bkey')+"&ckey="+$(ele).attr('ckey')+'&'+ gp;
    },
    toPiracyBookChapter:function(ele,gl) {
    	var id = $(ele).attr('bid');
    	var md = $(ele).attr('md');
    	var cmd = $(ele).attr('cmd');
    	location.href = 'http://k.sogou.com/novel/detail?v=5&md='+ md +'&id=' + id +'&cmd='+cmd+'&'+ gp;
    },
    updateRecomm:function updateRecommBooks (bkey,ele) {
    	$.ajax ({ 
    		type: 'post',
            dataType: 'json',
            data:{bkey:bkey},
            url: '/h5/cpt/ajax/detail/recommend?'+gp,
            success: function(data) {
            	if (data.status == 'succ') {
            		var items = data.list;
                	var _html = '';
                	$(ele).empty();
                	for (var i=0;i<items.length;i++) {
                		var item = items [i];
                		_html += "<td stats=\"1\" bkey=\""+item.bkey+"\" id=\"recommend_"+i+"\">";
                		_html += "<div class=\"book-detail\">";
                		_html += "<div class=\"book-cover\"><img src=\"/h5/images/bg156x216.jpg\" data-echo=\""+item.cover+"\" alt=\""+item.name+"\"></div>";
                		_html += "<h3 class=\"book-title\">"+fmtStr(item.name,8)+"</h3></div></td>";	
                	}
                	
                	$(ele).append(_html);
                	if(Echo){
                        Echo.destroy();
                        Echo.init();
                    }
            	}
            }
    	});
    },
    displaySortRecomm:function(json){
        var html="";
        if(json && json.status=='succ'){
            for(var i =0;i<json.list.length;i++ ){
                var item = json.list[i];
                var name = item.name;
                html+="<td stats=\"1\" bkey=\""+item.bkey+"\" id=\"recomm_list_"+i+"\">";
                html+="<div class=\"book-detail\">";
                html+="<div class=\"book-cover\"><img src=\"/h5/images/bg156x216.jpg\" data-echo=\""+item.cover+"\" alt=\""+item.name+"\"></div>";
                html+="<h3 class=\"book-title\" title='"+name+"'>"+(name.length>7?item.name.substr(0,6)+"..":name)+"</h3>";
                html+="</div>";
                html+="</td>";
            }
            $("#recomm_list").html(html);
            Echo.init();
            $('#recomm_list>td').on('click',function(){
                Geri.toCopyright(this);
            })
        }
    },
    displayCategory:function(json){
        var html="";
        if(json && json.status=='succ'){
            for(var i =0;i<json.list.length;i++ ){
                var item = json.list[i];
                html+="<li stats=\"1\" bkey=\""+item.bkey+"\" id=\"cate_list_"+i+"\"><div class=\"book-cover book-cover-size72\">";
                html+="<img src=\"/h5/images/bg156x216.jpg\" data-echo=\""+item.cover+"\" alt=\""+item.name+"\"></div>";
                html+="<div class=\"book-detail\">";
                html+="<h3 class=\"book-title\">"+item.name+"</h3>";
                html+="<p class=\"book-author\"><em>"+item.author+"</em> | <em>"+item.type+"</em></p>";
                html+="<p class=\"book-intro\">"+item.descr+"</p>";
                html+="</div></li>";
            }
            if(json.pn==1){
                $("#cate_list").html(html);
            }else{
                $("#cate_list").append(html);
            }
            var obj =$(".load-more");
            if(!json.hasNext){
                obj.text("没有更多");
                obj.data("on","false");
            }else{
                obj.text("加载更多");
                obj.data("on","true");
            }
            Echo.init();
            $('#cate_list>li').on('click',function(){
                Geri.toCopyright(this);
            })
        }
    },
    displayIndexFree:function (json) {
    	var html = "";
    	if(json && json.status=='succ'){
    		if(json.gender==1){
    			gender_en = 'girl';
    			gender_cn = '女';
    			to_gender_cn = '男';
    			to_gender = 0;
    			pre_id="boy_index_"
            } else {
            	gender_en = 'boy';
    			gender_cn = '男';
    			to_gender_cn = '女';
    			pre_id="girl_index_"
    			to_gender = 1;
            }
    		var time = getCountDown(json.list[0].limitedFree.end);
    		html += "<h2 class=\"module-title-free\"> <img src=\"./images/free/"+gender_en+".png\" alt=\""+gender_cn+"生免费\" /> " +
    				"<span class=\"free-count\">限时<strong>"+time[0]+"</strong>天<strong>"+time[1]+"</strong>:<strong>"+time[2]+"</strong>:<strong>"+time[3]+"</strong></span> " +
    				"<a href=\"javascript:void(0);\" class=\"free-change\">"+to_gender_cn+"生</a> </h2>";
    		html += "<ul class=\"list-col1\">";
    		for(var i =0 ;i<Math.min(4,json.list.length);i++ ){
                var item = json.list[i];
                html += "<li stats=\"1\" bkey=\""+item.bkey+"\" id=\""+pre_id+((json.pn-1)*json.pz+i)+"\">";
                if (i == 0) {
                	html += "<div class=\"book-cover sbs-free\"> <img src=\"/h5/images/bg156x216.jpg\" data-echo=\""+item.cover+"\" alt=\"\"> </div>";
                	html += "<div class=\"book-detail\">";
                	html += "	<div class=\"free-book-head\">";
                	html += "		<div class=\"free-name\">";
                	html += "	<h3 class=\"free-name-book\">"+item.name+"</h3>";
                	html += "</div> <del>"+item.price+"</del> </div>";
                	html += "<p class=\"book-summary\">"+item.descr+"</p>";
                	html += "<div class=\"book-info\">"+item.author+"<em class=\"book-tag5\">"+item.type+"</em></div>";
                	if (item.amount!='' && !isNaN(Number(item.amount))) {
                		var num = parseInt(item.amount);
                		num = num>10000?(num/10000).toFixed(1)+'万':num;
                		html += "<div class=\"book-view\">"+num+"人在看</div></div>";
                	}
    				
                	html += "</li> ";
                } else {
            		html += "<div class=\"book-detail\">";
            		html += "<div class=\"free-book-head\">";
            		html += "<div class=\"free-name\">";
            		html += "<h3 class=\"free-name-book\">"+item.name+"</h3> <span class=\"free-name-author\">"+item.author+"</span> </div> <del>"+item.price+"</del> </div>";
            		html += "</div>";
            		html += "</li>";
                }
            }
    		
    		html += "</ul>";
    		html += "<div class=\"free-enter\"> <a href=\"/h5/free?gender="+json.gender+"&"+gp+"\">进入免费频道</a> </div>";
    		
    		$('.module-free').empty().append(html);
    		
    		Echo.init();
            $('.list-col1>li').on('click',function(){
                Geri.toCopyright(this);
            })
            $('a.free-change').on('click',function(){
            	var url = "/h5/ajax/indexFree?"+gp;
                var param = {"gender":to_gender};
                clearInterval(timer);
            	postGeriJson(url,param,Geri.displayIndexFree,true);
            });
            timer = setInterval(function(){
                countDown($('.module-title-free .free-count strong'));
            },1000);
    	}
    },
    displayFree:function(json){
        var html="";
        
        if(json && json.status=='succ'){
            var pre_id="boy_2_"
            if(json.gender==1){
                pre_id = "girl_2_";
            }
            for(var i =0;i<json.list.length;i++ ){
                var item = json.list[i];
                html+="<li stats=\"1\" bkey=\""+item.bkey+"\" id=\""+pre_id+((json.pn-1)*json.pz+i)+"\"><div class=\"book-cover book-cover-size72\">";
                html+="<img src=\"/h5/images/bg156x216.jpg\" data-echo=\""+item.cover+"\" alt=\""+item.name+"\"></div>";
                html+="<div class=\"book-detail\">";
                html+="<h3 class=\"book-title\">"+item.name+"</h3>";
                html+="<p class=\"book-author\"><em>"+item.author+"</em> | <em>"+item.type+"</em></p>";
                html+="<p class=\"book-intro\">"+item.descr+"</p>";
                html+="</div></li>";
            }
            var obj = null;
            if(json.gender==1){
                $("#girl_2").append(html);
                obj = $("#girl_load_more");
            }else{
                $("#boy_2").append(html);
                obj = $("#boy_load_more");
            }
            if(!json.hasNext){
                obj.text("没有更多");
                obj.data("on","false");
            }else{
                obj.html("加载更多<i class=\"icon-arrowhead-d\"></i>");
                obj.data("on","true");
            }
            Echo.init();
            $('#girl_2>li').on('click',function(){
                Geri.toCopyright(this);
            })
            $('#boy_2>li').on('click',function(){
                Geri.toCopyright(this);
            })
        }
    },
    setAutoPay:function(value){
        var sgid = CookieUtil.get("sgid");
        CookieUtil.set("auto-pay-"+sgid,value);
    },
    getAutoPay:function(){
        var sgid = CookieUtil.get("sgid");
        return CookieUtil.get("auto-pay-"+sgid);
    },
    callApp:function (bookName,bkey,author,id,downloadUrl) {
        var openUrl;
        if (navigator.userAgent.match(/iPhone|iPad|iPod/)) {
            var data = {
                "title": bookName,
                "author": author,
                "key": bkey
            };
            data = JSON.stringify(data);
            openUrl = "sogoureader://web?from=web&data=" + encodeURIComponent(data);
        } else {
            openUrl = "sogounovel://k.sogou.com/novel/detail?nn=" + bookName + "&md=" + bkey + "&id=" + id;
        }
        var callback =function(data){
            if(data == 0){
                window.location.href = downloadUrl;
            }
        }

        openApp(openUrl,callback);
    },
    checkLogin:function(){
        var sgid = CookieUtil.get("sgid");
        return sgid?true:false;;
    },
    getFuli:function(){
        var user_code = CookieUtil.get("user_code");
        var fl = CookieUtil.get("fl-"+user_code);
        return fl;
    },
    setFuli:function(value){
        var user_code = CookieUtil.get("user_code");
        CookieUtil.set("fl-"+user_code,value);
    },
    fuliDoit:function fuliDoit (sgid,obj,key) {
        $.ajax ({
            type: 'post',
            dataType: 'json',
            data:{sgid:sgid},
            url: '/h5/ajax/activity/reward?'+window.gp,
            success: function(data) {
                if (data.status == 'succ') {
                    GeriTip.toast("获得1000搜豆，请在账户中查收~","2000");
                    CookieUtil.set(key,"1");
                }else{
                    GeriTip.toast(data.message,"2000");
                    if(data.message=="已领取奖励"){
                        CookieUtil.set(key,"1");
                    }
                }
                obj.removeClass("done");
                obj.addClass("done");
                obj.text("已领取");
            }
        });
    },
    displayWexinSignin:function(json){
        var html="<a href=\"javascript:void(0)\" class=\"had-checkin\">已签到</a><p>已领取100搜豆，明日再来哟!</p>";
        if(json && json.status=='succ'){
            $(".signin-banner").html(html);
            $(".wenxin-signin-mask").show();
            Stats.cpb('id','pop_weixin_signin');
        }
    },
    displayPopReceive:function(json){
        if(json && json.status=='succ'){
            $("#re_button").hide();
            $("#red_button").show();
            Stats.cpb('id','pop_receive');
        }else{
            $("#re_button").show();
            $("#red_button").hide();
        }
    },
    setChapterClickCount:function(k,v){
        var d = new Date();
        d.setHours(23);
        d.setMinutes(59);
        d.setSeconds(59);
        CookieUtil.set(k,v,d);
    },
    getChapterClickCount:function(){
        var sgid = CookieUtil.get("sgid");
        var c = CookieUtil.get("pop-focus-"+sgid);
        return (c==undefined || c==null)?0:parseInt(c);
    },
    popFocus:function(_n){
        var sgid = CookieUtil.get("sgid");
        var _c = Geri.getChapterClickCount();
        if(_c<_n){
            Geri.setChapterClickCount("pop-focus-"+sgid,++_c);
        }else if(_c==_n){
            Geri.displayPopHmtl(_n);
        }
    },
    displayPopHmtl:function(_n){
        var sgid = CookieUtil.get("sgid");
        $("#pop_focus").show();
        Geri.setChapterClickCount("pop-focus-"+sgid,_n+1);
    },
    wxPay:function (amount,openid,ru) {
    	function onBridgeReady(){
    		$.ajax({
    			type:"post",
    			dataType:"json",
    			data:{openid:openid,amount:amount},
    			url:"/h5/ajax/pay/weixin/jsapi/create",
    			success:function(data){
    				WeixinJSBridge.invoke(
    			       'getBrandWCPayRequest', data.jsapi,
    			       function(res){
    			    	   window.location.href = ru;
    			       })
    			}
    		});
    	}
    	if (typeof WeixinJSBridge == "undefined"){
    	   if( document.addEventListener ){
    	       document.addEventListener('WeixinJSBridgeReady', onBridgeReady, false);
    	       
    	   }else if (document.attachEvent){
    	       document.attachEvent('WeixinJSBridgeReady', onBridgeReady); 
    	       document.attachEvent('onWeixinJSBridgeReady', onBridgeReady);
    	   }
    	}else{
    	   onBridgeReady();
    	}
    },
    popFocusCPB:function(key,value){
        var obj = $("#pop_focus_link");
        if(obj!=null && obj!=undefined && obj.size()>0){
            Stats.cpb(key,value);
        }
    }
}
