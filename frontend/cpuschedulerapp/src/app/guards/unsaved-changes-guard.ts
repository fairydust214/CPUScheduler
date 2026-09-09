import { CanDeactivateFn } from '@angular/router';

export interface CanLeavePage {
  canDeactivate: () => boolean;
}

export const unsavedChangesGuard: CanDeactivateFn<CanLeavePage> = (component) => {
  return component.canDeactivate();
};
