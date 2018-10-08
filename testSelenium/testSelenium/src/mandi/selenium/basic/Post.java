package mandi.selenium.basic;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class Post {
	private static final AtomicInteger count = new AtomicInteger(0); 
	final long postId;
	String postContent;
	Date postDate;
	String likeNr = null;
	String loveNr = null;
	String hahaNr = null;
	String WowNr = null;
	String AngryNr = null;
	String SadNr = null;
	String CurrentUrl = null;
	int ChangePostDate = 0;
			
	
	public Post(Date postDate,String postContent,String likeNr, String loveNr, String hahaNr, String WowNr,
			String AngryNr, String SadNr,String currentUrl,int changePostDate) {
		this.postContent = postContent;
		this.postDate = postDate;
		this.likeNr = likeNr;
		this.loveNr = loveNr;
		this.hahaNr = hahaNr;
		this.WowNr = WowNr;
		this.AngryNr = AngryNr;
		this.SadNr = SadNr;
		this.CurrentUrl = currentUrl;
		postId =  count.incrementAndGet(); 
		this.ChangePostDate = changePostDate;
	}
	
	public String getPostId() {
		String postID = Long.toString(postId);
		return postID;
	}
	
	public Date getPostTime() {
		return postDate;
	}
	
	public void setPostTime(Date postDate) {
		this.postDate = postDate;
	}

	public String getPostContent() {
		return postContent;
	}
	
	public void setPostContent(String postContent) {
		this.postContent = postContent;
	}
	
	public String getLikeNr() {
		return likeNr;
	}
	
	public void setLikeNr(String likeNr) {
		this.likeNr = likeNr;
	}
	
	public String getLoveNr() {
		return loveNr;
	}
	
	public void setLoveNr(String loveNr) {
		this.loveNr = loveNr;
	}

	public String getHahaNr() {
		return hahaNr;
	}
	
	public void setHahaNr(String hahaNr) {
		this.hahaNr = hahaNr;
	}
	
	public String getWowNr() {
		return WowNr;
	}
	
	public void setWowNr(String WowNr) {
		this.WowNr = WowNr;
	}
	
	public String getAngryNr() {
		return AngryNr;
	}
	
	public void setAngryNr(String angryNr) {
		this.AngryNr = angryNr;
	}
	
	public String getSadNr() {
		return SadNr;
	}
	
	public void setSadNr(String sadNr) {
		this.SadNr = sadNr;
	}
	
	public String getCurrentUrl() {
		return CurrentUrl;
	}
	
	public void setCurrentUrl(String currentUrl) {
		this.CurrentUrl = currentUrl;
	}
	
	public int getChangePostDate() {
		return ChangePostDate;
	}
	
	public void setChangePostDate(int changePostDate) {
		this.ChangePostDate = changePostDate;
	}
}
