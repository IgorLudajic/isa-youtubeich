import UploadForm from "@/components/UploadForm";
import { getProfile } from "@/lib/auth";
import { redirect } from "next/navigation";

export default async function UploadPage() {
  const profile = await getProfile();

  if (!profile) {
    redirect("/login");
  }

  return (
    <div className="min-h-[80vh] bg-grid p-4 md:p-8 flex items-center justify-center">
      <UploadForm />
    </div>
  );
}
