# WireGuard VPN Demo

A demo Android application showcasing how to integrate and control a **WireGuard VPN** tunnel on Android using Kotlin and Android’s native `VpnService`.

![App Screenshot](https://cdn-images-1.medium.com/v2/resize:fit:2560/1*QnK7EoCtXMMNYNSgjXjpYQ.png)

Read the full article on [Medium](https://medium.com/@satish.nada98).

---

## 📘 Overview
This project demonstrates how to build a simple VPN client that establishes a secure connection using **WireGuard protocol**.  
It’s ideal for learning how to implement a WireGuard VPN, test configurations, or build production-ready VPN applications.

---

## ✨ Features
- Start and stop WireGuard VPN tunnel from Android app  
- Setup WireGuard configuration 
- Monitor connection status (Connected / Disconnected)  
- Built with **Kotlin**, **MVVM architecture**, and **Android VPN APIs**  
- Lightweight and easy to extend  

---

## 🧰 Tech Stack
- **Language:** Kotlin  
- **Networking:** WireGuard native implementation  
- **Android Components:** VpnService, ViewModel
- **Build System:** Gradle (Kotlin DSL)
  
---

## ⚙️ Getting Started

### 1. Clone the repository
```bash
git clone https://github.com/satishnada/WireGuard-VPN-Demo.git
cd WireGuard-VPN-Demo
```

### 2. Open in Android Studio
- Open **Android Studio (latest version)**  
- Select **File → Open...** and choose the cloned project folder  
- Let Gradle sync dependencies automatically  

### 3. Configure WireGuard
- Add your WireGuard `.conf` file in the app’s assets or define it directly in code  
- Example configuration:
  ```ini
  [Interface]
  PrivateKey = <your_private_key>
  Address = 10.0.0.2/32
  DNS = 1.1.1.1

  [Peer]
  PublicKey = <peer_public_key>
  Endpoint = vpn.example.com:51820
  AllowedIPs = 0.0.0.0/0, ::/0
  ```
- Make sure your VPN endpoint is reachable.

### 4. Run the app
- Build and install the app on a **real Android device**  
- Tap **“Start VPN”** to activate the WireGuard tunnel  
- Accept the system VPN permission dialog  

---

## 🧩 Project Structure

```
WireGuard-VPN-Demo/
│
├── app/
│   ├── src/main/
│   │   ├── java/com/example/wireguarddemo/
│   │   │   ├── MainActivity.kt
│   │   │   ├── WireGuardService.kt
│   │   │   └── utils/
│   │   └── res/
│   ├── AndroidManifest.xml
│   └── build.gradle.kts
│
├── gradle/
├── settings.gradle.kts
└── README.md
```

---

## 🚀 Future Enhancements
- Dynamic server selection  
- Split tunneling and kill switch  
- Real-time logs and connection analytics  
- Multi-protocol support (WireGuard / OpenVPN)  
- UI improvements with Compose  

---

## 🤝 Contributing
Contributions are welcome!  
1. Fork the repository  
2. Create your feature branch (`git checkout -b feature/my-feature`)  
3. Commit your changes (`git commit -m 'Add feature'`)  
4. Push to the branch (`git push origin feature/my-feature`)  
5. Open a **Pull Request**

---

## 📄 License
This project is licensed under the **Apache 2.0 License** — see the [LICENSE](LICENSE) file for details.

---

> ⚡ *Secure your network with WireGuard — fast, simple, and modern!*
