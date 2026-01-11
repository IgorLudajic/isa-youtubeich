import SignupForm from "@/components/SignupForm";
import { getProfile } from "@/lib/auth";
import { redirect } from "next/navigation";
import GoBack from "@/components/GoBack";

export default async function Page() {
  const profile = await getProfile();
  if (profile) {
    redirect("/");
  }

  return (
    <div className="grow-1 flex flex-col justify-center items-center h-full">
      <div className="flex flex-col gap-2 min-w-sm">
        <GoBack className="w-fit" />
        <SignupForm />
      </div>
    </div>
  );
}
