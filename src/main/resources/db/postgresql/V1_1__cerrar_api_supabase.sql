-- Supabase publica por su API REST todas las tablas del esquema public, y la
-- clave anon que da acceso a esa API no es secreta. Con RLS activo y sin
-- políticas, esa API no ve ni escribe nada. La app conecta como el dueño de
-- las tablas (postgres), que no está sujeto a RLS.
-- Solo corre en Postgres: H2 no conoce esta sintaxis.
alter table usuario enable row level security;
alter table salon   enable row level security;
alter table evento  enable row level security;
alter table orden   enable row level security;
alter table entrada enable row level security;
