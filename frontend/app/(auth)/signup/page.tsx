import Link from "next/link";
import SignupForm from "@/components/SignupForm";
import { getProfile } from "@/lib/auth";
import { redirect } from "next/navigation";

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

export default async function Page() {
  const profile = await getProfile();
  if (profile) {
    redirect("/");
  }

  return (
    <div className="grow-1 flex flex-col justify-center items-center h-full">
      <div className="flex flex-col gap-2 min-w-sm">
        <GoBack />
        <SignupForm />
      </div>
    </div>
  );
}
