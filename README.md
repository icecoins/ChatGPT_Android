# ChatGPT_Android
an android demo to use openai's api

调用openai提供的接口，实现与chatGPT对话

# !!!注意：https://api.openai.com 已经被GFW dns阻断了
最新版本可以选择使用服务器运行springboot+websocket与APP即时通讯，进行数据中转，以此绕开sni

配套springboot项目地址：https://github.com/icecoins/ChatGPT_Server

# 注意： 关于api_key失效
api_key一旦被官方检测到一个key被多人使用就会自动失效

一个openai账号可以维持最多五个api key，也就是说可以让五个人分别专用一个账号下不同的key


# usage
check the configs first, confirm your api_key is available.

connected to the Internet.

首先检查配置，确保api_key可用

建议将文本长度调整至1000及以上

确保APP可以联网
