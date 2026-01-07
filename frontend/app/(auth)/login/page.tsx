import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import Link from "next/link";
import LoginForm from "@/components/LoginForm";
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
        <Card className="w-full max-w-sm">
          <CardHeader className="select-none">
            <CardTitle>Uloguj se</CardTitle>
            <CardDescription>
              Unesite svoj mejl i lozinku da biste se ulogovali.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <LoginForm />
          </CardContent>
          <CardFooter className="flex-col gap-2">
            <div className="mt-4 text-center text-sm">
              Nemate svoj nalog?{" "}
              <Link href="/signup" className="underline underline-offset-4">
                Registrujte se ovde.
              </Link>
            </div>
          </CardFooter>
        </Card>
      </div>
    </div>
  );
}
