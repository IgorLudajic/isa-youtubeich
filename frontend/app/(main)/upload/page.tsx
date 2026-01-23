import UploadForm from "@/components/UploadForm";
import { getProfile } from "@/lib/auth";
import { redirect } from "next/navigation";

export default async function UploadPage() {
  const profile = await getProfile();

  if (!profile) {
    redirect("/login");
  }

  return (
    <div className="mx-auto flex items-center justify-center max-w-screen-xl">
      <UploadForm />
    </div>
  );
}
