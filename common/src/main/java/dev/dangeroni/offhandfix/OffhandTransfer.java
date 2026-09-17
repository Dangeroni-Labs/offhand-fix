package dev.dangeroni.offhandfix;

public final class OffhandTransfer {
    private OffhandTransfer() {
    }

    public static int calculateAmount(int offhandCount, int maxStackSize, int sourceCount) {
        int space = maxStackSize - offhandCount;
        if (space <= 0) {
            return 0;
        }

        int transferable = Math.min(sourceCount, space);
        if (transferable <= 0) {
            return 0;
        }

        return transferable;
    }
}
