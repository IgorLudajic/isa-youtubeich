"use client";

import { useRouter } from "next/navigation";
import { cn } from "@/lib/utils";

export default function GoBack({ className }: { className?: string }) {
  const router = useRouter();

  const handleGoBack = () => {
    if (window.history.length <= 1) {
      router.push("/");
      return;
    }

    // Check if the previous history item is the current page
    if (document.referrer === window.location.href) {
      // Previous is the same page, skip it and go back further
      if (window.history.length > 2) {
        window.history.go(-2);
      } else {
        router.push("/");
      }
    } else {
      window.history.back();
    }
  };

  return (
    <button
      onClick={handleGoBack}
      className={cn(
        "text-sm cursor-pointer underline-offset-4 decoration-1 hover:underline decoration-wavy text-left pl-2",
        className,
      )}
    >
      ← Vrati se
    </button>
  );
}
