export default async function addSlide(presentation, ctx) {
  const slide = presentation.slides.add();

  const colors = {
    ink: "#0F172A",
    muted: "#475569",
    navy: "#0B2E4A",
    teal: "#00A3A3",
    bg: "#FFFFFF",
    border: "#E2E8F0",
    chip: "#F1F5F9",
  };

  ctx.addShape(slide, { x: 0, y: 0, w: ctx.W, h: ctx.H, fill: colors.bg });
  ctx.addShape(slide, { x: 0, y: 0, w: ctx.W, h: 10, fill: colors.teal });

  ctx.addText(slide, {
    x: 56,
    y: 34,
    w: ctx.W - 112,
    h: 44,
    text: "Architecture (High Level)",
    fontSize: 30,
    bold: true,
    color: colors.navy,
    typeface: ctx.fonts.title,
  });

  // Diagram layout
  const boxH = 64;
  const left = 90;
  const right = ctx.W - 90;
  const midY1 = 160;
  const gapY = 86;

  function box(x, y, w, h, title, subtitle) {
    ctx.addShape(slide, { x, y, w, h, fill: "#FFFFFF", line: ctx.line(colors.border, 1) });
    ctx.addText(slide, { x: x + 16, y: y + 10, w: w - 32, h: 22, text: title, fontSize: 16, bold: true, color: colors.ink });
    if (subtitle) {
      ctx.addText(slide, { x: x + 16, y: y + 34, w: w - 32, h: 22, text: subtitle, fontSize: 12, color: colors.muted });
    }
  }

  function arrow(x1, y1, x2, y2) {
    const thickness = 3;
    const dx = x2 - x1;
    const dy = y2 - y1;
    if (Math.abs(dx) > Math.abs(dy)) {
      const y = y1 - thickness / 2;
      ctx.addShape(slide, { x: Math.min(x1, x2), y, w: Math.abs(dx), h: thickness, fill: colors.teal });
      // arrow head
      const headX = dx > 0 ? x2 - 8 : x2;
      ctx.addShape(slide, { x: headX, y: y1 - 6, w: 8, h: 12, fill: colors.teal });
    } else {
      const x = x1 - thickness / 2;
      ctx.addShape(slide, { x, y: Math.min(y1, y2), w: thickness, h: Math.abs(dy), fill: colors.teal });
      const headY = dy > 0 ? y2 - 8 : y2;
      ctx.addShape(slide, { x: x1 - 6, y: headY, w: 12, h: 8, fill: colors.teal });
    }
  }

  const colW = 260;
  const svcW = 250;
  const clientX = left;
  const gatewayX = (ctx.W - colW) / 2;
  const servicesStartX = right - svcW;

  box(clientX, midY1, colW, boxH, "Frontend (React)", "Vite + Axios + Redux");
  box(gatewayX, midY1, colW, boxH, "API Gateway", "Spring Cloud Gateway");
  box(servicesStartX, midY1 - 54, svcW, boxH, "Auth Service", "JWT + Refresh, Redis, PostgreSQL");
  box(servicesStartX, midY1 + gapY - 54, svcW, boxH, "Product Service", "Catalog + Categories");
  box(servicesStartX, midY1 + 2 * gapY - 54, svcW, boxH, "Order Service", "Cart + Orders + Address");
  box(servicesStartX, midY1 + 3 * gapY - 54, svcW, boxH, "Payment Service", "PayPal integration");

  arrow(clientX + colW, midY1 + boxH / 2, gatewayX, midY1 + boxH / 2);
  arrow(gatewayX + colW, midY1 + boxH / 2, servicesStartX, midY1 + boxH / 2 - 54);
  arrow(gatewayX + colW, midY1 + boxH / 2, servicesStartX, midY1 + boxH / 2 + gapY - 54);
  arrow(gatewayX + colW, midY1 + boxH / 2, servicesStartX, midY1 + boxH / 2 + 2 * gapY - 54);
  arrow(gatewayX + colW, midY1 + boxH / 2, servicesStartX, midY1 + boxH / 2 + 3 * gapY - 54);

  // Shared infra row
  const infraY = 560;
  const infraX = 56;
  const infraW = ctx.W - 112;
  ctx.addShape(slide, { x: infraX, y: infraY, w: infraW, h: 112, fill: colors.chip, line: ctx.line(colors.border, 1) });
  ctx.addText(slide, { x: infraX + 18, y: infraY + 12, w: infraW - 36, h: 22, text: "Platform Components", fontSize: 14, bold: true, color: colors.ink });

  const chips = [
    "Service Discovery: Eureka",
    "Central Config: Config Server",
    "Caching: Redis",
    "DB: PostgreSQL",
    "Resilience: Resilience4j",
    "Observability: Actuator + logs (ELK ready)",
  ];
  ctx.addText(slide, {
    x: infraX + 18,
    y: infraY + 42,
    w: infraW - 36,
    h: 64,
    text: chips.join("  •  "),
    fontSize: 13,
    color: colors.muted,
  });

  ctx.addText(slide, {
    x: 56,
    y: ctx.H - 36,
    w: ctx.W - 112,
    h: 22,
    text: "Slide 3 / 7",
    fontSize: 11,
    color: "#94A3B8",
    align: "right",
  });

  return slide;
}

