package dev.com.simpleapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import org.jsoup.Jsoup
import java.util.regex.Pattern


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Observable.just("https://www.meitulu.com/t/nvshen/")
                .map {url->
                    val doc=Jsoup.connect(url).get()
                    val links=doc.select(".boxs .img li>a").map { ele->ele.attr("abs:href") }
                    var posts=ArrayList<Post>()
                    for (link in links){
                        posts.add(getPostInfo(link))
                    }
                    posts
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(Consumer { post -> println() })
    }
}
data class ImageItem(var url:String,var ratio:Float?=null)
data class Post(var url:String="",var images:List<ImageItem>?=null,var pageIndex:Byte =0,var pageCnt:Byte=0,var tags:List<String>?=null,var title:String="",var cnt:Int=0)

fun getPostInfo(link:String):Post{
    val doc=Jsoup.connect(link).get()
    var post=Post()
    post.url=doc.location()
    post.images=doc.select(".content center img").map { ele->
        var src=ele.attr("abs:src")
        ImageItem(url = src)
    }

    post.tags=doc.select("#fenxiang .fenxiang_l >a").map { ele->ele.text() }
    var postPagination=doc.select("#pages>a")
    if(postPagination.size>=2){
        post.pageCnt=postPagination.get(postPagination.size-2).text().toByte()
    }
    var metaInfos=doc.select(".width .c_l >p")
    if(metaInfos.size>=3){
        for(info in metaInfos){
            var str=info.text()
            if(str.startsWith("图片数量")){
                val regEx = "[^0-9]"
                val p = Pattern.compile(regEx)
                val m = p.matcher(str)
                post.cnt= m.replaceAll("").trim().toInt()
                break;
            }
        }
    }
    return post
}