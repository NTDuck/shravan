import re

def update():
    with open('overleaf/v2/main.tex', 'r', encoding='utf-8') as f:
        content = f.read()

    # 1. Tóm tắt
    content = content.replace(
        "TensorFlow Lite để chạy mô hình nhận diện vật thể (YOLOv5) và nhận diện tiền Việt Nam trực tiếp trên thiết bị (offline)",
        "TensorFlow Lite để chạy mô hình nhận diện vật thể (YOLOv5) trực tiếp trên thiết bị (offline)"
    )

    # 2. Mục tiêu dự án
    content = content.replace(
        "Ứng dụng sử dụng Jetpack Compose cho giao diện, CameraX cho luồng camera, TensorFlow Lite cho nhận diện vật thể và tiền Việt Nam, ML Kit OCR cho nhận dạng văn bản",
        "Ứng dụng sử dụng Jetpack Compose cho giao diện, CameraX cho luồng camera, TensorFlow Lite cho nhận diện vật thể, ML Kit OCR cho nhận dạng văn bản"
    )

    # 3. Phạm vi
    content = content.replace(
        "Phạm vi chức năng bao gồm chế độ khám phá vật thể, chế độ tìm vật thể, chế độ đọc văn bản, chế độ nhận diện tiền và phần cài đặt khả năng tiếp cận. Dự án tập trung vào trải nghiệm sử dụng trong sinh hoạt hằng ngày như đọc nhãn thuốc, hóa đơn, nhận biết đồ vật trong phòng và phân biệt tiền mặt.",
        "Phạm vi chức năng bao gồm chế độ khám phá vật thể, chế độ tìm vật thể, chế độ đọc văn bản và phần cài đặt khả năng tiếp cận. Dự án tập trung vào trải nghiệm sử dụng trong sinh hoạt hằng ngày như đọc nhãn thuốc, hóa đơn và nhận biết đồ vật trong phòng."
    )

    # 4. Luồng điều hướng
    content = content.replace(
        "Explore / Find / OCR / Currency / Settings / History",
        "Explore / Find / OCR / Settings / History"
    )
    
    # 5. Yêu cầu chức năng
    content = content.replace(
        "Ứng dụng cần nhận diện vật thể phổ biến bằng YOLOv5, nhận diện tiền Việt Nam bằng mô hình TensorFlow Lite chuyên biệt và cung cấp chế độ tìm vật thể với phản hồi rung theo mức độ gần đúng.",
        "Ứng dụng cần nhận diện vật thể phổ biến bằng YOLOv5 và cung cấp chế độ tìm vật thể với phản hồi rung theo mức độ gần đúng."
    )
    
    # 6. Các lệnh giọng nói (2 places)
    content = content.replace(
        "Các lệnh như ``Khám phá'', ``Tìm kiếm'', ``Văn bản'', ``Tiền'', ``Cài đặt'' và ``Quay lại''",
        "Các lệnh như ``Khám phá'', ``Tìm kiếm'', ``Văn bản'', ``Cài đặt'' và ``Quay lại''"
    )
    content = content.replace(
        "``Văn bản'', ``Tìm kiếm'', ``Tiền'', hoặc ``Quay lại''",
        "``Văn bản'', ``Tìm kiếm'', hoặc ``Quay lại''"
    )

    # 7. Bảng architecture
    content = content.replace(
        "Explore/Find/OCR/Currency/Settings/History",
        "Explore/Find/OCR/Settings/History"
    )
    # Using simple replace for the row to avoid regex escape issues
    content = content.replace(
        "RoboflowAnalyzer & Phân tích bằng Roboflow API, dùng nhận diện tiền (Currency). \\\\ \hline\n",
        ""
    )

    # 8. Công nghệ
    content = content.replace(
        "TensorFlow Lite chạy các mô hình nhận diện vật thể và tiền Việt Nam trực tiếp trên thiết bị.",
        "TensorFlow Lite chạy các mô hình nhận diện vật thể trực tiếp trên thiết bị."
    )
    content = content.replace(
        "Mô hình nhận diện vật thể chính là yolov5s-fp16.tflite, được tối ưu kích thước nhỏ và nhận diện 80 lớp COCO. Mô hình currency.tflite nhận diện tiền Việt Nam từ 1.000 đến 500.000 đồng.",
        "Mô hình nhận diện vật thể chính là yolov5s-fp16.tflite, được tối ưu kích thước nhỏ và nhận diện 80 lớp COCO."
    )

    # 9. VoiceCommandManager
    content = content.replace(
        "``ocr''/``văn bản'', ``currency''/``tiền'', ``settings''/``cài đặt''",
        "``ocr''/``văn bản'', ``settings''/``cài đặt''"
    )

    # 10. Đánh giá - nhiệm vụ
    content = content.replace(
        "5 nhiệm vụ: thiết lập lần đầu và chuyển chế độ, đọc nhãn thuốc bằng OCR, tìm một chiếc ghế bằng Find mode, nhận diện tờ 100.000 đồng và tạm dừng rồi tiếp tục đọc một văn bản dài.",
        "4 nhiệm vụ: thiết lập lần đầu và chuyển chế độ, đọc nhãn thuốc bằng OCR, tìm một chiếc ghế bằng Find mode và tạm dừng rồi tiếp tục đọc một văn bản dài."
    )

    # 11. Bảng Đánh giá
    content = re.sub(r'Nhận diện tờ 100\.000 đồng.*?\\\\', '', content)

    # 12. Thảo luận
    content = content.replace(
        "Nhận diện tiền hoạt động tốt với các mệnh giá phổ biến trong thử nghiệm, nhưng vẫn cần mở rộng dữ liệu cho tờ tiền cũ, nhàu hoặc bị che một phần. Ngoài ra, logic đọc thứ tự văn bản nhiều cột cần cải thiện",
        "Ngoài ra, logic đọc thứ tự văn bản nhiều cột cần cải thiện"
    )
    
    content = content.replace(
        "đặc biệt là OCR tiếng Việt, TTS tiếng Việt và nhận diện tiền Việt Nam. Seeing AI là giải pháp mạnh cho nhiều thị trường, nhưng hỗ trợ tiếng Việt và tiền Việt Nam không phải trọng tâm chính.",
        "đặc biệt là OCR tiếng Việt và TTS tiếng Việt. Seeing AI là giải pháp mạnh cho nhiều thị trường, nhưng hỗ trợ tiếng Việt không phải trọng tâm chính."
    )
    
    # Thêm workflows
    workflows = r"""
\section{Các luồng hoạt động chi tiết}

\subsection{Luồng hoạt động 1: Điều hướng bằng giọng nói}

Tính năng điều hướng bằng giọng nói cho phép người dùng thay đổi trạng thái của ứng dụng mà không cần tương tác chạm với màn hình. Khi người dùng phát âm lệnh (ví dụ: ``khám phá''), tín hiệu âm thanh được \texttt{VoiceCommandManager} thu nhận và đối chiếu với tập intent sẵn có. Nếu có sự trùng khớp, bộ quản lý này sẽ phát đi tín hiệu yêu cầu \texttt{MainScreen} thực hiện chuyển đổi giao diện hiển thị. Ngay khi luồng chuyển hoàn tất, \texttt{TTSManager} sẽ thông báo trạng thái hiện tại (ví dụ: ``Đang ở chế độ khám phá''), giúp người khiếm thị nhận thức ngay lập tức về sự thay đổi của hệ thống.

\begin{figure}[H]
    \centering
    \begin{tikzpicture}[>=stealth, auto, thick]
        % Actors/Components
        \node (user) at (0,0) [draw, fill=blue!10, minimum width=2.5cm, minimum height=0.8cm] {Người dùng};
        \node (vcm) at (4.5,0) [draw, fill=blue!10, minimum width=3.5cm, minimum height=0.8cm] {VoiceCommandManager};
        \node (main) at (9.5,0) [draw, fill=blue!10, minimum width=2.5cm, minimum height=0.8cm] {MainScreen};
        
        % Lifelines
        \draw[dashed] (user.south) -- (0,-5);
        \draw[dashed] (vcm.south) -- (4.5,-5);
        \draw[dashed] (main.south) -- (9.5,-5);
        
        % Activations
        \draw[->] (0,-1) -- node[above, font=\small] {Nói(``khám phá'')} (4.5,-1);
        \draw[->] (4.5,-2) -- +(1.5,0) |- node[right, font=\small, text width=2cm, align=left] {Khớp intent} (4.5,-2.7);
        \draw[->] (4.5,-3.5) -- node[above, font=\small] {navigateTo(Explore)} (9.5,-3.5);
        \draw[->, dashed] (9.5,-4.5) -- node[above, font=\small] {TTS(``Đang ở chế độ khám phá'')} (0,-4.5);
    \end{tikzpicture}
    \caption{Biểu đồ tuần tự: Điều hướng bằng giọng nói}
\end{figure}

\subsection{Luồng hoạt động 2: Tự động chụp và đọc văn bản (OCR)}

Quá trình quét và nhận dạng văn bản hoạt động liên tục dựa trên luồng khung hình từ \texttt{CameraX}. Các khung hình được đưa vào \texttt{OCRManager} để đánh giá độ nét và tính ổn định. Khi ứng dụng xác nhận văn bản đã nằm rõ ràng trong khung hình mà không có sự xê dịch đáng kể trong khoảng thời gian nhất định (cơ chế auto-capture), chuỗi ký tự sẽ được xuất ra và đưa vào \texttt{TTSManager}. Quá trình đọc văn bản được hỗ trợ các cử chỉ vuốt từ người dùng, cho phép nhảy sang câu tiếp theo, nghe lại câu trước, hoặc tạm dừng, mang lại sự linh hoạt tối đa khi tiếp cận văn bản dài.

\begin{figure}[H]
    \centering
    \begin{tikzpicture}[>=stealth, auto, thick]
        % Actors/Components
        \node (cam) at (0,0) [draw, fill=blue!10, minimum width=2.2cm, minimum height=0.8cm] {CameraX};
        \node (ocr) at (4,0) [draw, fill=blue!10, minimum width=2.5cm, minimum height=0.8cm] {OCRManager};
        \node (tts) at (8,0) [draw, fill=blue!10, minimum width=2.5cm, minimum height=0.8cm] {TTSManager};
        \node (user) at (12,0) [draw, fill=blue!10, minimum width=2.2cm, minimum height=0.8cm] {Người dùng};
        
        % Lifelines
        \draw[dashed] (cam.south) -- (0,-6.5);
        \draw[dashed] (ocr.south) -- (4,-6.5);
        \draw[dashed] (tts.south) -- (8,-6.5);
        \draw[dashed] (user.south) -- (12,-6.5);
        
        % Arrows
        \draw[->] (0,-1) -- node[above, font=\small] {Luồng khung hình} (4,-1);
        \draw[->] (4,-1.8) -- +(1.5,0) |- node[right, font=\small, text width=2.5cm, align=left] {Kiểm tra nét \& ổn định} (4,-2.5);
        \draw[->] (4,-3.3) -- node[above, font=\small] {speak(sentences)} (8,-3.3);
        \draw[->, dashed] (8,-4.1) -- node[above, font=\small] {Đọc câu đầu tiên} (12,-4.1);
        \draw[->] (12,-4.9) -- node[above, font=\small] {Vuốt phải (Next)} (8,-4.9);
        \draw[->, dashed] (8,-5.7) -- node[above, font=\small] {Đọc câu tiếp theo} (12,-5.7);
    \end{tikzpicture}
    \caption{Biểu đồ tuần tự: Tự động chụp và đọc văn bản bằng OCR}
\end{figure}

\subsection{Luồng hoạt động 3: Chế độ tìm vật thể và phản hồi xúc giác}

Khi người dùng muốn tìm một loại vật thể cụ thể, lệnh giọng nói (ví dụ: ``tìm ghế'') sẽ được phân giải để thiết lập nhãn đối tượng (``seatings'') cho \texttt{YoloAnalyzer}. Thuật toán YOLO liên tục rà soát các khung hình từ camera. Nếu phát hiện ra đối tượng, dữ liệu về vị trí và khoảng cách của vật thể đối với tâm khung hình sẽ được chuyển tới \texttt{HapticManager}. Thành phần này sẽ tính toán tần số nhịp rung tương ứng. Vật thể càng nằm sát vị trí giữa camera, nhịp rung sẽ càng dồn dập, cung cấp tín hiệu phản hồi không dây giúp người khiếm thị tự định vị đối tượng một cách trực quan.

\begin{figure}[H]
    \centering
    \begin{tikzpicture}[>=stealth, auto, thick]
        \node (user) at (0,0) [draw, fill=blue!10, minimum width=2cm, minimum height=0.8cm] {Người dùng};
        \node (vcm) at (3.5,0) [draw, fill=blue!10, minimum width=3cm, minimum height=0.8cm] {VoiceCmdMgr};
        \node (yolo) at (7.5,0) [draw, fill=blue!10, minimum width=2.5cm, minimum height=0.8cm] {YoloAnalyzer};
        \node (haptic) at (11.5,0) [draw, fill=blue!10, minimum width=2.5cm, minimum height=0.8cm] {HapticManager};
        
        \draw[dashed] (user.south) -- (0,-5.5);
        \draw[dashed] (vcm.south) -- (3.5,-5.5);
        \draw[dashed] (yolo.south) -- (7.5,-5.5);
        \draw[dashed] (haptic.south) -- (11.5,-5.5);
        
        \draw[->] (0,-1) -- node[above, font=\small] {Nói(``tìm ghế'')} (3.5,-1);
        \draw[->] (3.5,-2) -- node[above, font=\small] {setTarget(``seatings'')} (7.5,-2);
        \draw[->] (7.5,-2.8) -- +(1.2,0) |- node[right, font=\small, text width=2.2cm, align=left] {Phân tích khung hình} (7.5,-3.5);
        \draw[->] (7.5,-4.3) -- node[above, font=\small] {Phát hiện (khoảng cách)} (11.5,-4.3);
        \draw[->, dashed] (11.5,-5.1) -- node[above, font=\small] {Nhịp rung (Haptic Pulse)} (0,-5.1);
    \end{tikzpicture}
    \caption{Biểu đồ tuần tự: Chế độ tìm vật thể và phản hồi xúc giác}
\end{figure}

\chapter{Đánh giá khả năng sử dụng}"""

    content = content.replace(r"\chapter{Đánh giá khả năng sử dụng}", workflows)

    with open('overleaf/v2/main.tex', 'w', encoding='utf-8') as f:
        f.write(content)

update()
