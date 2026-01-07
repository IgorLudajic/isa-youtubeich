"use client";

import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { useForm } from "react-hook-form";
import { login } from "@/lib/auth";

export default function LoginForm() {
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    setError,
  } = useForm();

  const onSubmit = handleSubmit(async (data) => {
    try {
      await login(data.username, data.password);
      // Redirect or handle success
    } catch (error) {
      if ((error as { message: string }).message === "NEXT_REDIRECT") {
        // Success, redirect is happening
        return;
      }
      setError("password", {
        type: "manual",
        message: "Neispravno korisničko ime ili lozinka",
      });
    }
  });

  return (
    <form id="login-form" onSubmit={onSubmit}>
      <div className="flex flex-col gap-6">
        <div className="grid gap-2">
          <Label htmlFor="username">Korisničko ime</Label>
          <Input
            id="username"
            type="text"
            placeholder="korisnicko_ime"
            className="select-none"
            disabled={isSubmitting}
            {...register("username", {
              required: "Korisničko ime je obavezno",
            })}
          />
          {errors.username && (
            <p className="text-red-700 text-sm">
              {errors.username.message as string}
            </p>
          )}
        </div>
        <div className="grid gap-2">
          <div className="flex items-center">
            <Label htmlFor="password">Lozinka</Label>
            <a
              href="#"
              className="ml-auto inline-block text-sm underline-offset-4 hover:underline text-emerald-700"
            >
              Zaboravili ste lozinku?
            </a>
          </div>
          <Input
            id="password"
            type="password"
            disabled={isSubmitting}
            className="select-none"
            {...register("password", {
              required: "Lozinka je obavezna",
            })}
          />
          {errors.password && (
            <p className="text-red-500 text-sm">
              {errors.password.message as string}
            </p>
          )}
        </div>
        <Button type="submit" className="w-full" disabled={isSubmitting}>
          Uloguj se
        </Button>
      </div>
    </form>
  );
}
