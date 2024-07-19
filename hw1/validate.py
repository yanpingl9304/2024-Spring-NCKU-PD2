#!/bin/python3

import subprocess
import os
import sys
import shutil

# 測試案例和答案的路徑列表
TESTCASES = [
    "/home/share/hw1/tc0",
    "/home/share/hw1/tc1",
    "/home/share/hw1/tc2",
    "/home/share/hw1/tc3",
    "/home/share/hw1/tc4",
    "/home/share/hw1/tc5"
]

ANSWERS = [
    "/home/share/hw1/ans0",
    "/home/share/hw1/ans1",
    "/home/share/hw1/ans2",
    "/home/share/hw1/ans3",
    "/home/share/hw1/ans4",
    "/home/share/hw1/ans5"
]

DIFF_DIR = os.path.join('.', 'diff')

# 檢查是否存在 RegExp.java 檔案
if not os.path.isfile("RegExp.java"):
    print("❌錯誤：RegExp.java 文件不存在")
    exit(1)

# 編譯 RegExp.java
if subprocess.call(["javac", "RegExp.java"]) == 0:
    print("編譯完成")
else:
    print("❌錯誤：編譯時發生錯誤")
    exit(1)

# 執行測試案例
def run_testcase(tc_number, testcase, answer):
    try:
        result = subprocess.run(["java", "RegExp", testcase, "abc", "b", "3"], stdout=subprocess.PIPE, text=True, timeout=60)
        diff_process = subprocess.run(["sdiff", "--ignore-trailing-space", "-w", "50", "-l", "-", answer], input=result.stdout, text=True, capture_output=True)
        if diff_process.returncode == 0:
            print("✅")
        else:
            # Save output of diff to file
            diff_log = os.path.join('diff', answer.split('/')[-1] + "_diff")

            with open(diff_log, 'w') as f:
                f.write(diff_process.stdout)
            print(f"❌\t| 結果已存至 '{diff_log}'")
    except FileNotFoundError:
        print("找不到 sdiff 命令")
    except subprocess.TimeoutExpired:
        print("❌執行超時")
        sys.exit(1)  # 終止程式


if __name__ == '__main__':
    # Remove files in DIFF_DIR
    if os.path.exists(DIFF_DIR):
        shutil.rmtree(DIFF_DIR)
    os.mkdir(DIFF_DIR)
    
    # 如果沒有傳遞參數，預設執行所有測試案例
    if len(sys.argv) == 1:
        for i, (testcase, answer) in enumerate(zip(TESTCASES, ANSWERS)):
            print(f"testcase{i}: ", end='')
            run_testcase(i, testcase, answer)
    else:
        # 如果傳遞了參數，只執行指定編號的測試案例
        testcase_number = int(sys.argv[1])
        if 0 <= testcase_number < len(TESTCASES):
            print(f"testcase{testcase_number}: ", end='')
            run_testcase(testcase_number, TESTCASES[testcase_number], ANSWERS[testcase_number])
        else:
            print("❌錯誤：無效的測試案例編號")
            exit(1)
    
