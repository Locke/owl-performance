// SPDX-License-Identifier: MIT
package de.athalis.owl.performance;

class Util {
    protected static String niceTime(long durationNanoSeconds) {
        long seconds = Math.round(durationNanoSeconds / 1e9);
        if (seconds > 10) {
            return String.format("%02d:%02d", seconds / 60, seconds % 60);
        }
        else {
            return Math.round(durationNanoSeconds / 1e6) + " ms";
        }
    }
}
