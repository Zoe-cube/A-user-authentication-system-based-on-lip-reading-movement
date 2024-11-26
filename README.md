# A-user-authentication-system-based-on-lip-reading-movement


based on signals caputured on mobile phone(Android phone).


为了防止用户的隐私泄露，越来越多的移动设备采用基于生物识别的身份认证方法，如指纹、人脸识别、语音识别等，以增强隐私保护。然而，这些方法对重放攻击十分脆弱。虽然最新的解决方案利用活体检测来对抗攻击，但现有方法受环境影响较大，如环境光线和周围噪声。为此，本研究探索利用用户说话时的口唇运动进行用户身份认证的活体验证，该方法对嘈杂环境和环境光线具有鲁棒性。


本研究实现了一种基于唇读的用户身份认证系统，首先利用智能手机上内置的音频设备发送超声波，之后基于用户唇部运动时的反射信号提取用户说话时唇部的独特行为特征进行用户身份认证。本文首先研究了用户说话时唇部运动引起的声学信号的多普勒谱，发现不同个体具有独特的唇部运动模式。之后基于深度神经网络实现用户身份识别验证，在用户识别方面实现86.4％的准确度，并在欺骗者检测方面实现81.8％的准确度。
