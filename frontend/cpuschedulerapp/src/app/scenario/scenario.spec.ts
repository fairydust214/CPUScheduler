import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Scenario } from './scenario';

describe('Scenario', () => {
  let component: Scenario;
  let fixture: ComponentFixture<Scenario>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Scenario],
    }).compileComponents();

    fixture = TestBed.createComponent(Scenario);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
