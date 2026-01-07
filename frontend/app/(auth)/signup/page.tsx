import Link from "next/link";
import SignupForm from "@/components/SignupForm";

function GoBack() {
  return (
    <Link
      href={"/"}
      className="text-sm underline-offset-4 decoration-1 hover:underline decoration-wavy text-left pl-2"
    >
      ← Vrati se
    </Link>
  );
}

export default function Page() {
  return (
    <div className="grow-1 flex flex-col justify-center items-center h-full">
      <div className="flex flex-col gap-2 min-w-sm">
        <GoBack />
        <SignupForm />
      </div>
    </div>
  );
}
