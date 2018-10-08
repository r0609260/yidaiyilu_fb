package mandi.selenium.basic;

public class Website {
	private String websiteName;
	private int isPage;
	
	public Website(String websiteName, int isPage) {
		this.websiteName = websiteName;
		this.isPage = isPage;
	}
	
	public String getWebsiteName() {
		return websiteName;
	}
	
	public void setWebsiteName(String websiteName) {
		this.websiteName = websiteName;
	}
	
	public int getIsPage() {
		return isPage;
	}
	
	public void setIsPage(int isPage) {
		this.isPage = isPage;
	}
}
