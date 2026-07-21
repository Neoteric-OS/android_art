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

  // The parallel move resolver reuses an already-materialized constant for
  // further moves of the same constant ("constant source update"): after
  // (C -> reg1), a later (C -> reg2) becomes the register move (reg1 -> reg2).
  // That saves work for constants that are expensive to materialize (literal
  // pool doubles, MOVZ+MOVK chains), but for constants the macro assembler
  // materializes with a single instruction (zero, MOVZ/MOVN/logical-immediate
  // values, FMOV-encodable FP immediates) the rewrite only adds a register
  // dependency. Check that cheap constants are materialized independently
  // and expensive constants still reuse the first materialization.

  public static long $noinline$sum2(long a, long b) {
    return a + b;
  }

  /// CHECK-START-ARM64: long Main.$noinline$callTwoZeros() disassembly (after)
  /// CHECK:      mov x{{[12]}}, #0x0
  /// CHECK-NEXT: mov x{{[12]}}, #0x0
  public static long $noinline$callTwoZeros() {
    return $noinline$sum2(0L, 0L);
  }

  /// CHECK-START-ARM64: long Main.$noinline$callTwoOnes() disassembly (after)
  /// CHECK:      mov x{{[12]}}, #0x1
  /// CHECK-NEXT: mov x{{[12]}}, #0x1
  public static long $noinline$callTwoOnes() {
    return $noinline$sum2(1L, 1L);
  }

  // -1 materializes with a single MOVN.

  /// CHECK-START-ARM64: long Main.$noinline$callTwoMinusOnes() disassembly (after)
  /// CHECK:      mov x{{[12]}}, #0xffffffffffffffff
  /// CHECK-NEXT: mov x{{[12]}}, #0xffffffffffffffff
  public static long $noinline$callTwoMinusOnes() {
    return $noinline$sum2(-1L, -1L);
  }

  // 0x123456789 needs a MOVZ+MOVK chain, so the second argument still
  // reuses the first materialization with a register move.

  /// CHECK-START-ARM64: long Main.$noinline$callTwoBigConstants() disassembly (after)
  /// CHECK:      movk x{{[12]}},
  /// CHECK:      mov x{{[12]}}, x{{[12]}}
  public static long $noinline$callTwoBigConstants() {
    return $noinline$sum2(0x123456789L, 0x123456789L);
  }

  public static void main(String[] args) {
    long zeros = $noinline$callTwoZeros();
    if (zeros != 0L) {
      throw new Error("callTwoZeros: expected 0, got " + zeros);
    }
    long ones = $noinline$callTwoOnes();
    if (ones != 2L) {
      throw new Error("callTwoOnes: expected 2, got " + ones);
    }
    long minusOnes = $noinline$callTwoMinusOnes();
    if (minusOnes != -2L) {
      throw new Error("callTwoMinusOnes: expected -2, got " + minusOnes);
    }
    long bigs = $noinline$callTwoBigConstants();
    if (bigs != 0x2468ACF12L) {
      throw new Error("callTwoBigConstants: expected 0x2468ACF12, got " + bigs);
    }
    System.out.println("passed");
  }
}
