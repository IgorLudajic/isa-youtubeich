import { Button } from "@/components/ui/button";
import Link from "next/link";

export default function LoginButton() {
  return (
    <Button>
      <Link href={"/login"}>Uloguj se</Link>
    </Button>
  );
}
