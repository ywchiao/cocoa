本頁面是關於 GitHub Wiki 的 撰寫時的小撇步或注意事項。

##  嵌入圖片
GitHub 的圖片嵌入預設要給一個 URL，除了網上的圖片外，如果想要用自己的圖片，就需要執行以下步驟:
* `git clone https://github.com/ywchiao/cocoa.wiki.git`
* 將圖片放到專案內的 img/
* git add . && git commit
* git push origin master
* 在編輯 wiki (markdown) 時，圖片的 URL 為 https://raw.githubusercontent.com/wiki/ywchiao/cocoa/img/your-img-file-name。

  例如: `![I love cats](https://raw.githubusercontent.com/wiki/ywchiao/cocoa/img/i-love-cats.png)`
