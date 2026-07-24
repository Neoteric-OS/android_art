/*
 * Copyright (C) 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class Main {
  public static void main(String[] args) {
    expectEquals(28, $noinline$fanOutArgs(7));
    expectEquals(-28L, $noinline$fanOutLong(-7L));
    expectEquals(10.0d, $noinline$fanOutDouble(2.5d));
    expectEquals(117, $noinline$rotateArgs(25, 9, 4));
    expectEquals(5, $noinline$swappedArgs(4, 9));
    System.out.println("passed");
  }

  static int $noinline$sum3(int a, int b, int c) {
    return a + b + c;
  }

  static long $noinline$sumLong3(long a, long b, long c) {
    return a + b + c;
  }

  static double $noinline$sumDouble3(double a, double b, double c) {
    return a + b + c;
  }

  static int $noinline$weighted(int a, int b, int c) {
    return a + 2 * b + 4 * c;
  }

  static int $noinline$diff(int a, int b) {
    return a - b;
  }

  static void $noinline$nop() {}

  // A value that is live across a call and then passed as several arguments
  // fans out from its callee-save home. All argument copies must read that
  // home register directly instead of chaining on one another. The leading
  // call forces the value into its callee-save home early, so the argument
  // moves are the only register copies between the two calls.

  /// CHECK-START-ARM64: int Main.$noinline$fanOutArgs(int) disassembly (after)
  /// CHECK:      blr
  /// CHECK:      mov {{x\d+}}, <<Src:x\d+>>
  /// CHECK-NEXT: mov {{x\d+}}, <<Src>>
  /// CHECK-NEXT: mov {{x\d+}}, <<Src>>
  /// CHECK:      blr
  static int $noinline$fanOutArgs(int x) {
    $noinline$nop();
    int acc = $noinline$sum3(x, x, x);
    return acc + x;
  }

  /// CHECK-START-ARM64: long Main.$noinline$fanOutLong(long) disassembly (after)
  /// CHECK:      blr
  /// CHECK:      mov {{x\d+}}, <<Src:x\d+>>
  /// CHECK-NEXT: mov {{x\d+}}, <<Src>>
  /// CHECK-NEXT: mov {{x\d+}}, <<Src>>
  /// CHECK:      blr
  static long $noinline$fanOutLong(long x) {
    $noinline$nop();
    long acc = $noinline$sumLong3(x, x, x);
    return acc + x;
  }

  /// CHECK-START-ARM64: double Main.$noinline$fanOutDouble(double) disassembly (after)
  /// CHECK:      blr
  /// CHECK:      fmov {{d\d+}}, <<Src:d\d+>>
  /// CHECK-NEXT: fmov {{d\d+}}, <<Src>>
  /// CHECK-NEXT: fmov {{d\d+}}, <<Src>>
  /// CHECK:      blr
  static double $noinline$fanOutDouble(double x) {
    $noinline$nop();
    double acc = $noinline$sumDouble3(x, x, x);
    return acc + x;
  }

  // Rotating and swapping argument registers still forms dependency cycles;
  // these only verify at runtime that cycle resolution stays correct.

  static int $noinline$rotateArgs(int a, int b, int c) {
    return $noinline$weighted(b, c, a);
  }

  static int $noinline$swappedArgs(int a, int b) {
    return $noinline$diff(b, a);
  }

  private static void expectEquals(int expected, int result) {
    if (expected != result) {
      throw new Error("Expected: " + expected + ", found: " + result);
    }
  }

  private static void expectEquals(long expected, long result) {
    if (expected != result) {
      throw new Error("Expected: " + expected + ", found: " + result);
    }
  }

  private static void expectEquals(double expected, double result) {
    if (expected != result) {
      throw new Error("Expected: " + expected + ", found: " + result);
    }
  }
}
